package jlc.main.Instructions.x86.IR;

import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableKind;
import jlc.main.Variables.VariableType;

import java.util.ArrayList;
import java.util.List;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Address;
import jlc.main.Instructions.x86.CodeGenHelper;
import jlc.main.Instructions.x86.LivenessAnalysis;
import jlc.main.Instructions.x86.Operand;
import jlc.main.Instructions.x86.Register;
import jlc.main.Instructions.x86.Utils;
import jlc.main.Instructions.x86.Instructions.X86MoveFPInstruction;
import jlc.main.Instructions.x86.Instructions.X86MoveInstruction;

/**
 * Represents a memory-to-register load in the x86 IR:
 *   move dest, [address]
 *
 */
public class LoadIR implements IR {
    private Variable dest;
    private Variable address;

    public LoadIR() {}

    public LoadIR(Variable dest, Variable address) {
        setOperands(dest, address);
    }

    public void setOperands(Variable dest, Variable address) {
        this.dest = dest;
        this.address = address;
    }

    @Override
    public String GetIR() {
        if (dest == null || address == null) {
            throw new IllegalStateException("LoadIR operands not fully initialized");
        }

        return String.format("LOAD %s, %s",
                dest.GetVariableName(),
                address.GetVariableName());
    }

    @Override
    public void PerformLivenessAnalysis(LivenessAnalysis livenessAnalysis) {
        if (address != null && Utils.isVirtualVariable(address)) {
            livenessAnalysis.recordVar(address);
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

        Operand memOp;
        if (Utils.isGlobalVariable(address)) {
            memOp = Operand.ofGlobal(address.GetVariableName(), address.GetVariableType());
        } else if (address.GetVariableKind() == VariableKind.ConstantVariable) {
            memOp = Operand.ofImmediate(address);
        } else {
            memOp = Operand.ofMemory(address);
        }

        Register rd = codeGenHelper.getRegisterFor(dest);
        Operand destOp;
        if (rd != null) {
            destOp = Operand.of(rd);
        } else if (dest.GetVariableType() == VariableType.Double) {
            destOp = Operand.of(Register.xmmScratch());
        } else {
            destOp = Operand.of(Register.gpScratch());
        }

        if (dest.GetVariableType() == VariableType.Double) {
            X86MoveFPInstruction mv = new X86MoveFPInstruction(
                true,
                destOp, memOp
            );
            mv.AddNumOfSpaceForPrefix(4);
            out.add(mv);
        } else {
            X86MoveInstruction mv = new X86MoveInstruction(destOp, memOp);
            mv.AddNumOfSpaceForPrefix(4);
            out.add(mv);
        }

        codeGenHelper.spillIfNeeded(dest, destOp, out);

        codeGenHelper.finishStep();
        return out;
    }
}
