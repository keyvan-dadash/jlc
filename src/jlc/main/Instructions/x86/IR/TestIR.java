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
import jlc.main.Instructions.x86.Instructions.X86TestInstruction;

/**
 * Represents an x86 TEST instruction in the IR, which performs a bitwise AND
 * of two operands and sets the CPU flags based on the result.
 *
 * Commonly used to check if a variable is non-zero by testing it against itself:
 *   TEST src, src
 *
 * You can also test two different variables or values:
 *   TEST src1, src2
 */
public class TestIR implements IR {
    private Variable src1;
    private Variable src2;

    /** Uninitialized constructor. You must call setOperands(...) before GetIR(). */
    public TestIR() {}

    /**
     * Construct a TEST of a single variable against itself:
     *   TEST var, var
     *
     * @param var the variable to test for non-zero
     */
    public TestIR(Variable var) {
        setOperands(var, var);
    }

    /**
     * Construct a TEST of two operands:
     *   TEST src1, src2
     *
     * @param src1 the first operand
     * @param src2 the second operand
     */
    public TestIR(Variable src1, Variable src2) {
        setOperands(src1, src2);
    }

    /**
     * Set or reset the operands for this TEST instruction.
     *
     * @param src1 the first operand
     * @param src2 the second operand
     */
    public void setOperands(Variable src1, Variable src2) {
        this.src1 = src1;
        this.src2 = src2;
    }

    @Override
    public String GetIR() {
        if (src1 == null || src2 == null) {
            throw new IllegalStateException("TestIR operands not fully initialized");
        }
        // Format: TEST src1, src2
        return String.format("TEST %s, %s",
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

        // 1) Materialize src1/src2 into registers or scratch if spilled
        Operand op1 = codeGenHelper.ensureInRegister(src1, out);
        Operand op2 = codeGenHelper.ensureInRegister(src2, out);

        // 2) Choose integer TEST vs FP compare
        if (src1.GetVariableType() == VariableType.Double) {
            // 32-bit “double” → use unordered compare (ucomiss)
            X86CmpFPInstruction cmpfp = new X86CmpFPInstruction(
                /* isDouble = */ true,
                op1, op2
            );
            cmpfp.AddNumOfSpaceForPrefix(4);
            out.add(cmpfp);
        } else {
            // int/boolean → TEST
            X86TestInstruction test = new X86TestInstruction(op1, op2);
            test.AddNumOfSpaceForPrefix(4);
            out.add(test);
        }

        // 3) Advance the step counter
        codeGenHelper.finishStep();
        return out;
    }
}
