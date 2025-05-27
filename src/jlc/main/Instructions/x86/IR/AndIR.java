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
import jlc.main.Instructions.x86.Instructions.X86AndInstruction;
import jlc.main.Instructions.x86.Instructions.X86MoveInstruction;

/**
 * Represents a three‐operand bitwise AND in the x86 IR:
 *     dest = src1 & src2
 */
public class AndIR implements IR {
    private final Variable dest;
    private final Variable src1;
    private final Variable src2;

    /**
     * @param dest the variable that will receive the result
     * @param src1 the first operand
     * @param src2 the second operand
     */
    public AndIR(Variable dest, Variable src1, Variable src2) {
        this.dest = dest;
        this.src1 = src1;
        this.src2 = src2;
    }

    @Override
    public String GetIR() {
        // Emits something like: AND dest, src1, src2
        return String.format("AND %s, %s, %s",
                dest.GetVariableName(),
                src1.GetVariableName(),
                src2.GetVariableName());
    }

    @Override
    public void PerformLivenessAnalysis(LivenessAnalysis livenessAnalysis) {
        // record uses first
        if (src1 != null && Utils.isVirtualVariable(src1)) {
            livenessAnalysis.recordVar(src1);
        }
        if (src2 != null && Utils.isVirtualVariable(src2)) {
            livenessAnalysis.recordVar(src2);
        }
        // then record the definition
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

        // 1) Load src1/src2 into regs or scratch
        Operand op1 = codeGenHelper.ensureInRegister(src1, out);
        Operand op2 = codeGenHelper.ensureInRegister(src2, out);

        // 2) Pick dest operand (real reg or GP scratch)
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

        // 4) Byte-wise AND is exactly logical && on 0/1 booleans
        X86AndInstruction andInst = new X86AndInstruction(opDest, op2);
        andInst.AddNumOfSpaceForPrefix(4);
        out.add(andInst);

        // 5) If dest was spilled, write it back
        codeGenHelper.spillIfNeeded(dest, opDest, out);

        // 6) Advance the step
        codeGenHelper.finishStep();
        return out;
    }
}
