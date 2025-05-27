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
import jlc.main.Instructions.x86.Instructions.X86MoveFPInstruction;
import jlc.main.Instructions.x86.Instructions.X86MoveInstruction;

/**
 * Represents a two‐operand assignment in the x86 IR:
 *     dest = src
 */
public class AssIR implements IR {
    private final Variable dest;
    private final Variable src;

    /**
     * @param dest the variable that will receive the value
     * @param src  the variable whose value is assigned
     */
    public AssIR(Variable dest, Variable src) {
        this.dest = dest;
        this.src = src;
    }

    @Override
    public String GetIR() {
        // Emits something like: ASS dest, src
        return String.format("ASS %s, %s",
                dest.GetVariableName(),
                src.GetVariableName());
    }

    @Override
    public void PerformLivenessAnalysis(LivenessAnalysis livenessAnalysis) {
        // record use of src
        if (src != null && Utils.isVirtualVariable(src)) {
            livenessAnalysis.recordVar(src);
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

        // 1) Load src into a register (or scratch) if spilled
        Operand opSrc = codeGenHelper.ensureInRegister(src, out);

        // 2) Pick dest operand: its allocated reg or the appropriate scratch
        Register rd = codeGenHelper.getRegisterFor(dest);
        Operand opDest;
        if (!Utils.isAddressVariable(dest)) {
            if (rd != null) {
                opDest = Operand.of(rd);
            } else if (dest.GetVariableType() == VariableType.Double) {
                // spilled “double” (32-bit float) → use FP scratch
                opDest = Operand.of(Register.xmmScratch());
            } else {
                // spilled int/boolean → use GP scratch
                opDest = Operand.of(Register.gpScratch());
            }
        } else {
            opDest = Operand.ofMemory(dest);
        }

        // 3) Emit the correct move opcode
        if (dest.GetVariableType() == VariableType.Double) {
            // movss dest, src
            X86MoveFPInstruction mv = new X86MoveFPInstruction(
                /* isDouble= */ true,  // your “double” is 32-bit
                opDest, opSrc
            );
            mv.AddNumOfSpaceForPrefix(4);
            out.add(mv);
        } else {
            // mov dest, src
            X86MoveInstruction mv = new X86MoveInstruction(opDest, opSrc);
            mv.AddNumOfSpaceForPrefix(4);
            out.add(mv);
        }

        // 4) If dest was spilled, store it back
        // System.out.println(dest.GetVariableName());
        codeGenHelper.spillIfNeeded(dest, opDest, out);

        // 5) Advance to next IR step
        codeGenHelper.finishStep();
        return out;
    }
}
