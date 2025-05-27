package jlc.main.Instructions.x86.IR;

import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableType;

import java.util.ArrayList;
import java.util.List;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.CodeGenHelper;
import jlc.main.Instructions.x86.LivenessAnalysis;
import jlc.main.Instructions.x86.Operand;
import jlc.main.Instructions.x86.Utils;
import jlc.main.Instructions.x86.Instructions.X86CmpFPInstruction;
import jlc.main.Instructions.x86.Instructions.X86TestInstruction;

/**
 * Represents an x86 TEST instruction in the IR, which performs a bitwise AND
 * of two operands and sets the CPU flags based on the result.
 *
 */
public class TestIR implements IR {
    private Variable src1;
    private Variable src2;

    public TestIR() {}

    public TestIR(Variable var) {
        setOperands(var, var);
    }

    public TestIR(Variable src1, Variable src2) {
        setOperands(src1, src2);
    }

    public void setOperands(Variable src1, Variable src2) {
        this.src1 = src1;
        this.src2 = src2;
    }

    @Override
    public String GetIR() {
        if (src1 == null || src2 == null) {
            throw new IllegalStateException("TestIR operands not fully initialized");
        }

        return String.format("TEST %s, %s",
                src1.GetVariableName(),
                src2.GetVariableName());
    }

    @Override
    public void PerformLivenessAnalysis(LivenessAnalysis livenessAnalysis) {

        if (src1 != null && Utils.isVirtualVariable(src1)) {
            livenessAnalysis.recordVar(src1);
        }
        if (src2 != null && Utils.isVirtualVariable(src2)) {
            livenessAnalysis.recordVar(src2);
        }

        livenessAnalysis.finishStep();
    }

    @Override
    public List<Instruction> GenerateX86Code(CodeGenHelper codeGenHelper) {
        List<Instruction> out = new ArrayList<>();

        codeGenHelper.spillCurrentStep(out);

        Operand op1 = codeGenHelper.ensureInRegister(src1, out);
        Operand op2 = codeGenHelper.ensureInRegister(src2, out);

        if (src1.GetVariableType() == VariableType.Double) {
            X86CmpFPInstruction cmpfp = new X86CmpFPInstruction(
                true,
                op1, op2
            );
            cmpfp.AddNumOfSpaceForPrefix(4);
            out.add(cmpfp);
        } else {
            X86TestInstruction test = new X86TestInstruction(op1, op2);
            test.AddNumOfSpaceForPrefix(4);
            out.add(test);
        }

        codeGenHelper.finishStep();
        return out;
    }
}
