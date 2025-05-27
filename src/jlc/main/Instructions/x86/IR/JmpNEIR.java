package jlc.main.Instructions.x86.IR;

import java.util.ArrayList;
import java.util.List;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.CodeGenHelper;
import jlc.main.Instructions.x86.LivenessAnalysis;
import jlc.main.Instructions.x86.Instructions.X86JneInstruction;

/**
 * Represents a conditional jump-if-not-equal in the x86 IR:
 *     JNE label
 *
 * This should follow a CMP instruction and will jump to the given label
 * if the zero flag is not set (i.e., the operands compared were not equal).
 */
public class JmpNEIR implements IR {
    private String label;

    /**
     * Construct an uninitialized JmpNEIR.
     * You must call setLabel(...) before calling GetIR().
     */
    public JmpNEIR() {}

    /**
     * Construct a JmpNEIR with the target label.
     *
     * @param label the name of the label to jump to
     */
    public JmpNEIR(String label) {
        this.label = label;
    }

    /**
     * Set or reset the jump target label.
     *
     * @param label the name of the label to jump to
     */
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String GetIR() {
        if (label == null || label.isEmpty()) {
            throw new IllegalStateException("JmpNEIR label not initialized");
        }
        // Emit the conditional jump mnemonic and the label
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

        // emit “    jne label”
        X86JneInstruction jne = new X86JneInstruction(label);
        jne.AddNumOfSpaceForPrefix(4);
        out.add(jne);

        codeGenHelper.finishStep();
        return out;
    }
}
