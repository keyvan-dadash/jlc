package jlc.main.Instructions.x86.IR;

import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableType;

import java.util.ArrayList;
import java.util.List;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.CodeGenHelper;
import jlc.main.Instructions.x86.LivenessAnalysis;
import jlc.main.Instructions.x86.Operand;
import jlc.main.Instructions.x86.Register;
import jlc.main.Instructions.x86.Utils;
import jlc.main.Instructions.x86.Instructions.X86CmpFPInstruction;
import jlc.main.Instructions.x86.Instructions.X86CmpInstruction;

/**
 * Represents a compare instruction in the x86 IR:
 *     CMP src1, src2
 *
 * This sets the CPU flags based on (src1 - src2) but does not write a result.
 */
public class CmpIR implements IR {
    private Variable src1;
    private Variable src2;

    /**
     * Construct an uninitialized CmpIR.
     * You must call setOperands(...) before calling GetIR().
     */
    public CmpIR() {}

    /**
     * Construct a CmpIR with both operands.
     *
     * @param src1 the left operand for the comparison
     * @param src2 the right operand for the comparison
     */
    public CmpIR(Variable src1, Variable src2) {
        setOperands(src1, src2);
    }

    /**
     * Set or reset the operands for this compare instruction.
     *
     * @param src1 the left operand
     * @param src2 the right operand
     */
    public void setOperands(Variable src1, Variable src2) {
        this.src1 = src1;
        this.src2 = src2;
    }

    @Override
    public String GetIR() {
        if (src1 == null || src2 == null) {
            throw new IllegalStateException("CmpIR operands not fully initialized");
        }
        // Format: CMP src1, src2
        return String.format("CMP %s, %s",
                src1.GetVariableName(),
                src2.GetVariableName());
    }

    @Override
    public void PerformLivenessAnalysis(LivenessAnalysis livenessAnalysis) {
        // record uses of both operands
        if (src1 != null && Utils.isVirtualVariable(src1)) {
            livenessAnalysis.recordVar(src1);
        }
        if (src2 != null && Utils.isVirtualVariable(src2)) {
            livenessAnalysis.recordVar(src2);
        }
        // advance to next instruction index
        livenessAnalysis.finishStep();
    }

    @Override
    public List<Instruction> GenerateX86Code(CodeGenHelper codeGenHelper) {
        List<Instruction> out = new ArrayList<>();

        codeGenHelper.spillCurrentStep(out);

        // 1) Load src1 into a register (or scratch) if spilled
        Operand op1 = codeGenHelper.ensureInRegister(src1, out);

        // 2) Load src2 into a register (or scratch) if spilled
        Operand op2 = codeGenHelper.ensureInRegister(src2, out);

        // 3) Emit the appropriate compare instruction
        if (src1.GetVariableType() == VariableType.Double) {
            // 32-bit “double” ⇒ use SSE unordered compare (movss/ucomiss)
            X86CmpFPInstruction cmpfp = new X86CmpFPInstruction(
                /* isDouble = */ true,
                op1, op2
            );
            cmpfp.AddNumOfSpaceForPrefix(4);
            out.add(cmpfp);
        } else {
            // integer or boolean compare
            X86CmpInstruction cmp = new X86CmpInstruction(op1, op2);
            cmp.AddNumOfSpaceForPrefix(4);
            out.add(cmp);
        }

        // 4) Advance the IR step
        codeGenHelper.finishStep();
        return out;
    }
}
