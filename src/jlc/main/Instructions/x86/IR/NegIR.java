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
import jlc.main.Instructions.x86.Instructions.X86MoveInstruction;
import jlc.main.Instructions.x86.Instructions.X86NegInstruction;
import jlc.main.Instructions.x86.Instructions.X86SubFPInstruction;
import jlc.main.Instructions.x86.Instructions.X86XorFPInstruction;

/**
 * Represents a unary negation in the x86 IR:
 *     dest = NEG src
 */
public class NegIR implements IR {
    private final Variable dest;
    private final Variable src;

    /**
     * @param dest the variable that will receive the negated value
     * @param src  the variable to negate
     */
    public NegIR(Variable dest, Variable src) {
        this.dest = dest;
        this.src = src;
    }

    @Override
    public String GetIR() {
        // Emits something like: NEG dest, src
        return String.format("NEG %s, %s",
                dest.GetVariableName(),
                src.GetVariableName());
    }

    @Override
    public void PerformLivenessAnalysis(LivenessAnalysis livenessAnalysis) {
        // record use of src before it's overwritten
        if (src != null && Utils.isVirtualVariable(src)) {
            livenessAnalysis.recordVar(src);
        }
        // record definition of dest
        if (dest != null && Utils.isVirtualVariable(dest)) {
            livenessAnalysis.recordVar(dest);
        }
        // NEG has no special fixed-register requirements
        livenessAnalysis.finishStep();
    }

    @Override
    public List<Instruction> GenerateX86Code(CodeGenHelper codeGenHelper) {
        List<Instruction> out = new ArrayList<>();

        codeGenHelper.spillCurrentStep(out);

        // 1) Materialize src into a register or scratch if spilled
        Operand opSrc = codeGenHelper.ensureInRegister(src, out);

        // 2) Pick dest operand: real reg or proper scratch based on type
        Register rd = codeGenHelper.getRegisterFor(dest);
        Operand opDest;
        if (rd != null) {
            opDest = Operand.of(rd);
        } else if (dest.GetVariableType() == VariableType.Double) {
            // spilled 32-bit “double” → use FP scratch
            opDest = Operand.of(Register.xmmScratch());
        } else {
            // spilled int/boolean → use GP scratch
            opDest = Operand.of(Register.gpScratch());
        }

        // 3) Lower NEG:
        if (dest.GetVariableType() == VariableType.Double) {
            // FP neg: xorps dest, dest   ; sets zero
            X86XorFPInstruction zx = new X86XorFPInstruction(
                /* isDouble= */ true, opDest, opDest
            );
            zx.AddNumOfSpaceForPrefix(4);
            out.add(zx);

            // subss dest, src  => 0 - src
            X86SubFPInstruction sub = new X86SubFPInstruction(
                /* isDouble= */ true, opDest, opSrc
            );
            sub.AddNumOfSpaceForPrefix(4);
            out.add(sub);
        } else {
            // integer neg: mov dest, src; neg dest
            X86MoveInstruction mv = new X86MoveInstruction(opDest, opSrc);
            mv.AddNumOfSpaceForPrefix(4);
            out.add(mv);

            X86NegInstruction neg = new X86NegInstruction(opDest);
            neg.AddNumOfSpaceForPrefix(4);
            out.add(neg);
        }

        // 4) Spill dest back if needed
        codeGenHelper.spillIfNeeded(dest, opDest, out);

        // 5) Advance the IR step
        codeGenHelper.finishStep();
        return out;
    }

}
