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
 * Represents negation in the x86 IR.
 * There is not usecase for this.
 */
public class NegIR implements IR {
    private final Variable dest;
    private final Variable src;

    public NegIR(Variable dest, Variable src) {
        this.dest = dest;
        this.src = src;
    }

    @Override
    public String GetIR() {
        return String.format("NEG %s, %s",
                dest.GetVariableName(),
                src.GetVariableName());
    }

    @Override
    public void PerformLivenessAnalysis(LivenessAnalysis livenessAnalysis) {
        if (src != null && Utils.isVirtualVariable(src)) {
            livenessAnalysis.recordVar(src);
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

        Operand opSrc = codeGenHelper.ensureInRegister(src, out);

        Register rd = codeGenHelper.getRegisterFor(dest);
        Operand opDest;
        if (rd != null) {
            opDest = Operand.of(rd);
        } else if (dest.GetVariableType() == VariableType.Double) {
            opDest = Operand.of(Register.xmmScratch());
        } else {
            opDest = Operand.of(Register.gpScratch());
        }

        if (dest.GetVariableType() == VariableType.Double) {
            X86XorFPInstruction zx = new X86XorFPInstruction(
                true, opDest, opDest
            );
            zx.AddNumOfSpaceForPrefix(4);
            out.add(zx);

            X86SubFPInstruction sub = new X86SubFPInstruction(
                true, opDest, opSrc
            );
            sub.AddNumOfSpaceForPrefix(4);
            out.add(sub);
        } else {
            X86MoveInstruction mv = new X86MoveInstruction(opDest, opSrc);
            mv.AddNumOfSpaceForPrefix(4);
            out.add(mv);

            X86NegInstruction neg = new X86NegInstruction(opDest);
            neg.AddNumOfSpaceForPrefix(4);
            out.add(neg);
        }

        codeGenHelper.spillIfNeeded(dest, opDest, out);

        codeGenHelper.finishStep();
        return out;
    }

}
