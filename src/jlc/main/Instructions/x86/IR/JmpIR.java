package jlc.main.Instructions.x86.IR;

import java.util.ArrayList;
import java.util.List;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.CodeGenHelper;
import jlc.main.Instructions.x86.LivenessAnalysis;
import jlc.main.Instructions.x86.Instructions.X86JmpInstruction;

/**
 * Represents an unconditional jump in the x86 IR.
 *
 */
public class JmpIR implements IR {
    private final String label;

    public JmpIR(String label) {
        this.label = label;
    }

    @Override
    public String GetIR() {
        return String.format("JMP %s", label);
    }

    @Override
    public void PerformLivenessAnalysis(LivenessAnalysis livenessAnalysis) {
        livenessAnalysis.finishStep();
        return;
    }

    @Override
    public List<Instruction> GenerateX86Code(CodeGenHelper codeGenHelper) {
        List<Instruction> out = new ArrayList<>();

        codeGenHelper.spillCurrentStep(out);

        X86JmpInstruction jmp = new X86JmpInstruction(label);
        jmp.AddNumOfSpaceForPrefix(4);
        out.add(jmp);

        codeGenHelper.finishStep();
        return out;
    }
}
