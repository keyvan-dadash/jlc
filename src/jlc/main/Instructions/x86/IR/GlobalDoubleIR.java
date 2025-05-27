package jlc.main.Instructions.x86.IR;

import java.util.ArrayList;
import java.util.List;
import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.CodeGenHelper;
import jlc.main.Instructions.x86.LivenessAnalysis;
import jlc.main.Instructions.x86.Instructions.X86DirectiveInstruction;

/**
 * Represents a global double-precision constant in the x86 IR.
 *
 * This class'es goal is to save constant float as global  
 */
public class GlobalDoubleIR implements IR {
    private final String label;
    private final double value;

    public GlobalDoubleIR(String label, double value) {
        this.label = label;
        this.value = value;
    }

    @Override
    public String GetIR() {
        return String.format("GLOBALDOUBLE %s, %s", label, value);
    }

    @Override
    public void PerformLivenessAnalysis(LivenessAnalysis livenessAnalysis) {
        return;
    }

    @Override
    public List<Instruction> GenerateX86Code(CodeGenHelper helper) {
        List<Instruction> out = new ArrayList<>();
        out.add(new X86DirectiveInstruction(label + ":"));
        out.add(new X86DirectiveInstruction(String.format("    dq %s", value)));
        return out;
    }
}
