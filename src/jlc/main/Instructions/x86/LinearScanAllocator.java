package jlc.main.Instructions.x86;

import java.util.*;

import jlc.main.Instructions.x86.LivenessAnalysis.VarInterval;
import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableType;

/**
 * Linear-scan allocator with separate GP vs XMM active lists & pools for int and doubles.
 */
public class LinearScanAllocator {
    private final Deque<Register> freeGP;
    private final Deque<Register> freeXMM;
    private final Map<Variable,Integer> spillSlots = new HashMap<>();
    private final Map<Integer,List<Variable>> spillsByStep = new HashMap<>();

    public LinearScanAllocator() {
        freeGP  = new ArrayDeque<>(Arrays.asList(Register.gpForAllocations()));
        freeXMM = new ArrayDeque<>(Arrays.asList(Register.xmmForAllocations()));
    }

    public static class AssignedInterval {
        public final Variable var;
        public final int start, end;
        public final Register reg;
        public AssignedInterval(Variable v,int s,int e,Register r){
            var=v; start=s; end=e; reg=r;
        }
    }

    public static class AllocationResult {
        public final Map<Variable,List<AssignedInterval>> map;
        public AllocationResult(Map<Variable,List<AssignedInterval>> m){ map=m; }
    }

    public AllocationResult allocate(LivenessAnalysis la) {
        PriorityQueue<VarInterval> queue =
            new PriorityQueue<>(Comparator.comparingInt(VarInterval::getStart));
        queue.addAll(la.sortedIntervals());

        List<VarInterval> activeGP  = new ArrayList<>();
        List<VarInterval> activeXMM = new ArrayList<>();
        Map<Variable,List<AssignedInterval>> result = new HashMap<>();

        while (!queue.isEmpty()) {
            VarInterval cur = queue.poll();
            // expire old from correct list
            if (cur.getVariable().GetVariableType() == VariableType.Double) {
                expireOld(cur, activeXMM, freeXMM, result);
            } else {
                expireOld(cur, activeGP, freeGP, result);
            }

            // System.out.printf("Going to assign to %s\n", cur.getVariable().GetVariableName());

            // is fixed?
            Optional<Register> fix = la.getFixedRegister(cur.getVariable());
            if (fix.isPresent()) {
                // System.out.printf("fixed %s\n", cur.getVariable().GetVariableName());
                Register r = fix.get();
                if (cur.getVariable().GetVariableType() == VariableType.Double) {
                    freeXMM.remove(r);
                    assignFixed(cur, r, activeXMM, result);
                } else {
                    freeGP.remove(r);
                    assignFixed(cur, r, activeGP, result);
                }

                continue;
            }

            // choose pool/list based on the type of current variable
            boolean isFP = cur.getVariable().GetVariableType()==VariableType.Double;
            Deque<Register> freePool = isFP ? freeXMM : freeGP;
            // System.out.println(freePool.stream()
            //     .map(Register::getName)
            //     .collect(Collectors.joining(", ")));
            List<VarInterval> activeList = isFP ? activeXMM : activeGP;

            if (!freePool.isEmpty()) {
                // assign a free reg
                assign(cur, freePool.removeFirst(), activeList, result);
            } else {
                // try split the *last* in this activeList
                VarInterval last = activeList.get(activeList.size()-1);
                if (last.getEnd() > cur.getEnd() && getAssignedReg(last.getVariable(), result) != null) {
                    Register freed = splitInterval(last, cur.getStart(),
                                    activeList, result, queue);
                                    
                    freePool.addFirst(freed);
                    // now that reg is free, assign it
                    Register r = getAssignedReg(cur.getVariable(), result);
                    if (r==null) r = freePool.removeFirst();
                    assign(cur, r, activeList, result);
                    // System.err.printf("Spilling %s\n", last.getVariable().GetVariableName());
                    continue;
                }
                // spill it since the last is shorter
                spill(cur, result);
                recordSpillSlot(cur.getVariable());
                recordSpillStep(cur.getStart(), cur.getVariable());
                // System.err.println("Spill???");
            }
        }
        return new AllocationResult(result);
    }

    private void expireOld(VarInterval cur,
                           List<VarInterval> active,
                           Deque<Register> freePool,
                           Map<Variable,List<AssignedInterval>> result) {
        Iterator<VarInterval> it = active.iterator();
        while (it.hasNext()) {
            VarInterval iv = it.next();
            if (iv.getEnd() <= cur.getStart()) {
                // System.out.printf("%s %s\n", iv.getVariable().GetVariableName(), cur.getVariable().GetVariableName());
                Register r = getAssignedReg(iv.getVariable(), result);
                if (r != null) freePool.addLast(r);
                it.remove();
            }
        }
    }

    private void assign(VarInterval iv,
                        Register r,
                        List<VarInterval> active,
                        Map<Variable,List<AssignedInterval>> result) {
        active.add(iv);
        active.sort(Comparator.comparingInt(VarInterval::getEnd));
        result.computeIfAbsent(iv.getVariable(),k->new ArrayList<>())
              .add(new AssignedInterval(iv.getVariable(),iv.getStart(),iv.getEnd(),r));
        // System.err.printf("Assigned %s %s\n", iv.getVariable().GetVariableName(), r.getName());
    }

    private void assignFixed(VarInterval iv,
                             Register fixed,
                             List<VarInterval> active,
                             Map<Variable,List<AssignedInterval>> result) {
        active.removeIf(a -> fixed.equals(getAssignedReg(a.getVariable(),result)));
        assign(iv, fixed, active, result);
    }

    private Register splitInterval(VarInterval iv, int sp,
                               List<VarInterval> active,
                               Map<Variable,List<AssignedInterval>> result,
                               PriorityQueue<VarInterval> queue) {

        Register r = getAssignedReg(iv.getVariable(),result);

        List<AssignedInterval> recs = result.get(iv.getVariable());
        if (recs != null && !recs.isEmpty()) {
            // remove last entry because its gonna be splitted 
            recs.remove(recs.size() - 1);
        }

        int s=iv.getStart(), e=iv.getEnd();
        active.remove(iv);
        // head in reg
        if (s != sp) {
            VarInterval head = new VarInterval(iv.getVariable(), s, sp);
            assign(head, r, active, result);
        }
        // tail requeued (the spillted part)
        VarInterval tail = new VarInterval(iv.getVariable(), sp, e);
        spill(tail, result);
        recordSpillSlot(tail.getVariable());
        recordSpillStep(sp, tail.getVariable());

        queue.add(tail);

        return r;
    }

    private void spill(VarInterval iv,
                       Map<Variable,List<AssignedInterval>> result) {
        result.computeIfAbsent(iv.getVariable(),k->new ArrayList<>())
              .add(new AssignedInterval(iv.getVariable(),iv.getStart(),iv.getEnd(),null));
    }

    private void recordSpillSlot(Variable v) {
        if (!spillSlots.containsKey(v)) {
            spillSlots.put(v, 0);
        }
    }

    private Register getAssignedReg(Variable v,
                                    Map<Variable,List<AssignedInterval>> result) {
        List<AssignedInterval> list = result.get(v);
        if (list==null||list.isEmpty()) return null;
        return list.get(list.size()-1).reg;
    }

    public Map<Variable,Integer> getSpillSlots() {
        return spillSlots;
    }

    private void recordSpillStep(int step, Variable v) {
        spillsByStep
            .computeIfAbsent(step, k -> new ArrayList<>())
            .add(v);
    }

    public Map<Integer,List<Variable>> getSpillsByStep() {
        return Collections.unmodifiableMap(spillsByStep);
    }
}
