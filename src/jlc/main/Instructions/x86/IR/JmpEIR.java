package jlc.main.Instructions.x86.IR;

import java.util.ArrayList;
import java.util.List;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.CodeGenHelper;
import jlc.main.Instructions.x86.LivenessAnalysis;
import jlc.main.Instructions.x86.Instructions.X86JeInstruction;

/**
 * Represents a conditional jump-if-equal in the x86 IR:
 *     JE label
 *
 * This should follow a CMP (or TEST) instruction and will jump to the
 * given label if the zero flag *is* set (i.e., the operands compared were equal).
 */
public class JmpEIR implements IR {
    private String label;

    /** Uninitialized; you must call setLabel(...) before GetIR(). */
    public JmpEIR() {}

    /**
     * @param label the name of the label to jump to
     */
    public JmpEIR(String label) {
        this.label = label;
    }

    /**
     * Set or reset the jump‐target label.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String GetIR() {
        if (label == null || label.isEmpty()) {
            throw new IllegalStateException("JmpEIR label not initialized");
        }
        return String.format("JE %s", label);
    }

    @Override
    public void PerformLivenessAnalysis(LivenessAnalysis livenessAnalysis) {
        // no registers are used or defined here
        livenessAnalysis.finishStep();
    }

    @Override
    public List<Instruction> GenerateX86Code(CodeGenHelper codeGenHelper) {
        List<Instruction> out = new ArrayList<>();

        codeGenHelper.spillCurrentStep(out);
        
        X86JeInstruction je = new X86JeInstruction(label);
        je.AddNumOfSpaceForPrefix(4);
        out.add(je);
        codeGenHelper.finishStep();
        return out;
    }
}
