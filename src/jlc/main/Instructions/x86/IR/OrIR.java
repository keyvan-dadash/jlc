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

    /**
     * @param dest the variable that will receive the result
     * @param src1 the first operand
     * @param src2 the second operand
     */
    public OrIR(Variable dest, Variable src1, Variable src2) {
        this.dest = dest;
        this.src1 = src1;
        this.src2 = src2;
    }

    @Override
    public String GetIR() {
        // Emits something like: OR dest, src1, src2
        return String.format("OR %s, %s, %s",
                dest.GetVariableName(),
                src1.GetVariableName(),
                src2.GetVariableName());
    }

    @Override
    public void PerformLivenessAnalysis(LivenessAnalysis livenessAnalysis) {
        // record uses of src1 and src2
        if (src1 != null && Utils.isVirtualVariable(src1)) {
            livenessAnalysis.recordVar(src1);
        }
        if (src2 != null && Utils.isVirtualVariable(src2)) {
            livenessAnalysis.recordVar(src2);
        }
        // record definition of dest
        if (dest != null && Utils.isVirtualVariable(dest)) {
            livenessAnalysis.recordVar(dest);
        }
        // advance to next instruction index
        livenessAnalysis.finishStep();
    }

    @Override
    public List<Instruction> GenerateX86Code(CodeGenHelper codeGenHelper) {
        List<Instruction> out = new ArrayList<>();

        codeGenHelper.spillCurrentStep(out);

        // 1) Load src1/src2 into registers or scratch if spilled
        Operand op1 = codeGenHelper.ensureInRegister(src1, out);
        Operand op2 = codeGenHelper.ensureInRegister(src2, out);

        // 2) Pick dest operand: real reg or GP scratch if spilled
        Register rd = codeGenHelper.getRegisterFor(dest);
        Operand opDest = (rd != null)
            ? Operand.of(rd)
            : Operand.of(Register.gpScratch());

        // 3) Seed dest with src1 if needed
        if (rd == null || !rd.equals(codeGenHelper.getRegisterFor(src1))) {
            X86MoveInstruction mv = new X86MoveInstruction(opDest, op1);
            mv.AddNumOfSpaceForPrefix(4);
            out.add(mv);
        }

        // 4) Perform the OR
        X86OrInstruction orInst = new X86OrInstruction(opDest, op2);
        orInst.AddNumOfSpaceForPrefix(4);
        out.add(orInst);

        // 5) Spill dest back if it lives in memory
        codeGenHelper.spillIfNeeded(dest, opDest, out);

        // 6) Advance to the next IR step
        codeGenHelper.finishStep();
        return out;
    }
}
