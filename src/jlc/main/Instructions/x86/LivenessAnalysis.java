package jlc.main.Instructions.x86;

import java.util.*;

import jlc.main.Variables.Variable;

/**
 * Performs a simple liveness analysis over a sequence of IR instructions.
 *
 * Usage:
 *   LivenessAnalysis la = new LivenessAnalysis();
 *   // For each IR instruction in program order:
 *   //   insn.PerformLivenessAnalysis(la);
 *   //
 *   // After all instructions:
 *   //   List<VarInterval> intervals = la.sortedIntervals();
 *   //   la.debugPrintIntervals();
 *
 * You can also reserve registers for specific variables (e.g. return value in EAX):
 *   la.setFixedRegister(var, Register.EAX);
 */
public class LivenessAnalysis {
    private int cnt = 0;
    private final Map<Variable, List<Integer>> positions = new HashMap<>();
    private final Map<Variable, Register> fixedRegisters = new HashMap<>();

    /** Record that var is defined or used at the current instruction index. */
    public void recordVar(Variable var) {
        positions
            .computeIfAbsent(var, k -> new ArrayList<>())
            .add(cnt);
    }

    /** Advance to the next instruction index. Call after each IR instruction. */
    public void finishStep() {
        cnt++;
    }

    /** Assign a fixed physical register to a variable (e.g. return in EAX). */
    public void setFixedRegister(Variable var, Register reg) {
        fixedRegisters.put(var, reg);
    }

    /** Retrieve the fixed register for var, if one was set. */
    public Optional<Register> getFixedRegister(Variable var) {
        return Optional.ofNullable(fixedRegisters.get(var));
    }

    /**
     * Compute the live intervals [start, end] for each variable,
     * then return them sorted by increasing start index.
     */
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

    /**
     * Debug: print all live intervals to System.out
     */
    public void debugPrintIntervals() {
        List<VarInterval> intervals = sortedIntervals();
        System.out.println("Live Intervals (var: [start,end]):");
        for (VarInterval iv : intervals) {
            System.out.println("  " + iv);
        }
    }

    /** Simple holder for a variable’s live interval. */
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
