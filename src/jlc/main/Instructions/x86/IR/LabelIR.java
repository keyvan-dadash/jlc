package jlc.main.Instructions.x86.IR;

import java.util.ArrayList;
import java.util.List;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.CodeGenHelper;
import jlc.main.Instructions.x86.LivenessAnalysis;
import jlc.main.Instructions.x86.Instructions.X86LabelInstruction;

/**
 * Represents a label in the x86 IR, marking a basic‐block entry:
 *     label:
 *
 * You can construct it with a label name or set it later.
 */
public class LabelIR implements IR {
    private String label;

    /**
     * Construct an uninitialized LabelIR.
     * You must call setLabel(...) before calling GetIR().
     */
    public LabelIR() {}

    /**
     * Construct a LabelIR with the given label name.
     *
     * @param label the name of the label (without the trailing colon)
     */
    public LabelIR(String label) {
        this.label = label;
    }

    /**
     * Set or reset the label name.
     *
     * @param label the name of the label (without the trailing colon)
     */
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String GetIR() {
        if (label == null || label.isEmpty()) {
            throw new IllegalStateException("LabelIR label not initialized");
        }
        // Emit the label followed by a colon
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

        // e.g. emits “label:”
        X86LabelInstruction lbl = new X86LabelInstruction(label);
        // labels typically have no indent
        lbl.AddNumOfSpaceForPrefix(0);
        out.add(lbl);

        codeGenHelper.finishStep();
        return out;
    }
}