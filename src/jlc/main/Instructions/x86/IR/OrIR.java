package jlc.main.Instructions.x86.IR;

import jlc.main.Variables.Variable;

import java.util.ArrayList;
import java.util.List;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.CodeGenHelper;
import jlc.main.Instructions.x86.LivenessAnalysis;
import jlc.main.Instructions.x86.Operand;
import jlc.main.Instructions.x86.Register;
import jlc.main.Instructions.x86.Utils;
import jlc.main.Instructions.x86.Instructions.X86MoveInstruction;
import jlc.main.Instructions.x86.Instructions.X86OrInstruction;

/**
 * Represents a three‐operand bitwise OR in the x86 IR:
 *     dest = src1 | src2
 */
public class OrIR implements IR {
    private final Variable dest;
    private final Variable src1;
    private final Variable src2;

    public OrIR(Variable dest, Variable src1, Variable src2) {
        this.dest = dest;
        this.src1 = src1;
        this.src2 = src2;
    }

    @Override
    public String GetIR() {
        return String.format("OR %s, %s, %s",
                dest.GetVariableName(),
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

        if (dest != null && Utils.isVirtualVariable(dest)) {
            livenessAnalysis.recordVar(dest);
        }

        livenessAnalysis.finishStep();
    }

    @Override
    public List<Instruction> GenerateX86Code(CodeGenHelper codeGenHelper) {
        List<Instruction> out = new ArrayList<>();

        codeGenHelper.spillCurrentStep(out);

        Operand op1 = codeGenHelper.ensureInRegister(src1, out);
        Operand op2 = codeGenHelper.ensureInRegister(src2, out);

        Register rd = codeGenHelper.getRegisterFor(dest);
        Operand opDest = (rd != null)
            ? Operand.of(rd)
            : Operand.of(Register.gpScratch());

        if (rd == null || !rd.equals(codeGenHelper.getRegisterFor(src1))) {
            X86MoveInstruction mv = new X86MoveInstruction(opDest, op1);
            mv.AddNumOfSpaceForPrefix(4);
            out.add(mv);
        }

        X86OrInstruction orInst = new X86OrInstruction(opDest, op2);
        orInst.AddNumOfSpaceForPrefix(4);
        out.add(orInst);

        codeGenHelper.spillIfNeeded(dest, opDest, out);

        codeGenHelper.finishStep();
        return out;
    }
}
