package jlc.main.Instructions.x86;

import java.util.*;

import jlc.main.Variables.Variable;

/**
 * Performs a simple liveness analysis over a sequence of IR instructions.
 *
 */
public class LivenessAnalysis {
    private int cnt = 0;
    private final Map<Variable, List<Integer>> positions = new HashMap<>();
    private final Map<Variable, Register> fixedRegisters = new HashMap<>();

    public void recordVar(Variable var) {
        positions
            .computeIfAbsent(var, k -> new ArrayList<>())
            .add(cnt);
    }

    public void finishStep() {
        cnt++;
    }

    public void setFixedRegister(Variable var, Register reg) {
        fixedRegisters.put(var, reg);
    }

    public Optional<Register> getFixedRegister(Variable var) {
        return Optional.ofNullable(fixedRegisters.get(var));
    }

    public List<VarInterval> sortedIntervals() {
        List<VarInterval> intervals = new ArrayList<>();
        for (Map.Entry<Variable, List<Integer>> e : positions.entrySet()) {
            List<Integer> uses = e.getValue();
            if (uses.isEmpty()) continue;
            int start = Collections.min(uses);
            int end = Collections.max(uses);
            intervals.add(new VarInterval(e.getKey(), start, end));
        }
        intervals.sort(Comparator.comparingInt(VarInterval::getStart));
        return intervals;
    }

    public void debugPrintIntervals() {
        List<VarInterval> intervals = sortedIntervals();
        System.out.println("Live Intervals (var: [start,end]):");
        for (VarInterval iv : intervals) {
            System.out.println("  " + iv);
        }
    }

    public static class VarInterval {
        private final Variable var;
        private final int start;
        private final int end;

        public VarInterval(Variable var, int start, int end) {
            this.var = var;
            this.start = start;
            this.end = end;
        }

        public Variable getVariable() {
            return var;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        @Override
        public String toString() {
            return String.format("%s:[%d,%d]", var.GetVariableName(), start, end);
        }
    }
}
