package jlc.main.Instructions.x86.IR;

import jlc.main.Variables.Variable;

import java.util.ArrayList;
import java.util.List;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.CodeGenHelper;
import jlc.main.Instructions.x86.LivenessAnalysis;
import jlc.main.Instructions.x86.MemSize;
import jlc.main.Instructions.x86.Operand;
import jlc.main.Instructions.x86.Register;
import jlc.main.Instructions.x86.Utils;
import jlc.main.Instructions.x86.Instructions.X86MoveInstruction;
import jlc.main.Instructions.x86.Instructions.X86NotInstruction;
import jlc.main.Instructions.x86.Instructions.X86SetzInstruction;
import jlc.main.Instructions.x86.Instructions.X86TestInstruction;

/**
 * Represents a bitwise NOT in the x86‐style IR:
 *     dest = NOT src
 */
public class NotIR implements IR {
    private final Variable dest;
    private final Variable src;

    /**
     * @param dest the variable that will receive the bitwise‐negated value
     * @param src  the variable to negate
     */
    public NotIR(Variable dest, Variable src) {
        this.dest = dest;
        this.src = src;
    }

    @Override
    public String GetIR() {
        // Emits something like: NOT dest, src
        return String.format("NOT %s, %s",
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
        // No fixed-register requirements for NOT
        livenessAnalysis.finishStep();
    }

    @Override
    public List<Instruction> GenerateX86Code(CodeGenHelper codeGenHelper) {
        List<Instruction> out = new ArrayList<>();

        codeGenHelper.spillCurrentStep(out);

        // 1) Load src into a register or scratch if spilled
        Operand opSrc = codeGenHelper.ensureInRegister(src, out);

        X86TestInstruction x86TestInstruction = new X86TestInstruction(opSrc, opSrc);
        x86TestInstruction.AddNumOfSpaceForPrefix(4);
        out.add(x86TestInstruction);

        Operand opSrc8 = Operand.of(opSrc.getRegister().forMemSize(MemSize.BYTE));
        X86SetzInstruction x86SetzInstruction = new X86SetzInstruction(opSrc8);
        x86SetzInstruction.AddNumOfSpaceForPrefix(4);
        out.add(x86SetzInstruction);

        // 2) Pick dest operand: its real register or GP scratch if spilled
        Register rd = codeGenHelper.getRegisterFor(dest);
        Operand opDest = (rd != null)
            ? Operand.of(rd)
            : Operand.of(Register.gpScratch());

        X86MoveInstruction mv = new X86MoveInstruction(opDest, opSrc8);
        mv.AddNumOfSpaceForPrefix(4);
        out.add(mv);

        // 5) Spill dest back to memory if it was spilled
        codeGenHelper.spillIfNeeded(dest, opDest, out);

        // 6) Advance to next IR step
        codeGenHelper.finishStep();
        return out;
    }
}
