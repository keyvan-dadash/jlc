package jlc.main.Instructions.x86.IR;

import java.util.ArrayList;
import java.util.List;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.CodeGenHelper;
import jlc.main.Instructions.x86.LivenessAnalysis;
import jlc.main.Instructions.x86.Instructions.X86LabelInstruction;

/**
 * Represents a label in the x86 IR, marking a basic‐block entry.
 * 
 */
public class LabelIR implements IR {
    private String label;

    public LabelIR() {}

    public LabelIR(String label) {
        this.label = label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String GetIR() {
        if (label == null || label.isEmpty()) {
            throw new IllegalStateException("LabelIR label not initialized");
        }

        return String.format("%s:", label);
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

        X86LabelInstruction lbl = new X86LabelInstruction(label);
        lbl.AddNumOfSpaceForPrefix(0);
        out.add(lbl);

        codeGenHelper.finishStep();
        return out;
    }
}