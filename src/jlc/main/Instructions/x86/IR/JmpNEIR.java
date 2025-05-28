package jlc.main.Instructions.x86.IR;

import java.util.ArrayList;
import java.util.List;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.CodeGenHelper;
import jlc.main.Instructions.x86.LivenessAnalysis;
import jlc.main.Instructions.x86.Instructions.X86JneInstruction;

/**
 * Represents a conditional jump-if-not-equal in the x86 IR:
 * This will be mixed with test or cmp.
 *
 */
public class JmpNEIR implements IR {
    private String label;

    public JmpNEIR() {}

    public JmpNEIR(String label) {
        this.label = label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String GetIR() {
        if (label == null || label.isEmpty()) {
            throw new IllegalStateException("JmpNEIR label not initialized");
        }
        return String.format("JNE %s", label);
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

        X86JneInstruction jne = new X86JneInstruction(label);
        jne.AddNumOfSpaceForPrefix(4);
        out.add(jne);

        codeGenHelper.finishStep();
        return out;
    }
}
