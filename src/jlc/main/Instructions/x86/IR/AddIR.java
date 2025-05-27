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
import jlc.main.Instructions.x86.Instructions.X86AddFPInstruction;
import jlc.main.Instructions.x86.Instructions.X86AddInstruction;
import jlc.main.Instructions.x86.Instructions.X86MoveFPInstruction;
import jlc.main.Instructions.x86.Instructions.X86MoveInstruction;
import jlc.main.Instructions.x86.Instructions.X86SubFPInstruction;
import jlc.main.Instructions.x86.Instructions.X86SubInstruction;
import jlc.main.Operations.AddType;

/**
 * Represents a three‐operand arithmetic IR operation, either ADD or SUB:
 *    dest = src1 + src2    (if type == Plus)
 *    dest = src1 - src2    (if type == Minus)
 *
 */
public class AddIR implements IR {
    private final AddType type;
    private Variable dest;
    private Variable src1;
    private Variable src2;

    public AddIR(AddType type) {
        this.type = type;
    }

    public AddIR(AddType type, Variable dest, Variable src1, Variable src2) {
        this.type = type;
        setOperands(dest, src1, src2);
    }

    public void setOperands(Variable dest, Variable src1, Variable src2) {
        this.dest = dest;
        this.src1 = src1;
        this.src2 = src2;
    }

    @Override
    public String GetIR() {
        if (dest == null || src1 == null || src2 == null) {
            throw new IllegalStateException("AddIR operands not fully initialized");
        }
        String opcode;
        switch (type) {
            case Plus:  opcode = "ADD"; break;
            case Minus: opcode = "SUB"; break;
            default:    throw new IllegalArgumentException("Unsupported AddType: " + type);
        }

        return String.format("%s %s, %s, %s",
                opcode,
                dest.GetVariableName(),
                src1.GetVariableName(),
                src2.GetVariableName());
    }

    @Override
    public void PerformLivenessAnalysis(LivenessAnalysis livenessAnalysis) {
        if (src1 != null && Utils.isVirtualVariable(src1)) {
            livenessAnalysis.recordVar(src1);
        }
        if (src2 != null&& Utils.isVirtualVariable(src2)) {
            livenessAnalysis.recordVar(src2);
        }
        if (dest != null&& Utils.isVirtualVariable(dest)) {
            livenessAnalysis.recordVar(dest);
        }

        livenessAnalysis.finishStep();
    }

    @Override
    public List<Instruction> GenerateX86Code(CodeGenHelper codeGenHelper) {
        List<Instruction> out = new ArrayList<>();

        codeGenHelper.spillCurrentStep(out);

        // load operands
        Operand op1 = codeGenHelper.ensureInRegister(src1, out);
        Operand op2 = codeGenHelper.ensureInRegister(src2, out);

        // load dest operand
        Register rd = codeGenHelper.getRegisterFor(dest);
        Operand opDest;
        if (rd != null) {
            opDest = Operand.of(rd);
        } else if (dest.GetVariableType() == VariableType.Double) {
            opDest = Operand.of(Register.xmmScratch());   // XMM scratch
        } else {
            opDest = Operand.of(Register.gpScratch());   // GP scratch
        }

        // load based on the type
        if (rd == null || !rd.equals(codeGenHelper.getRegisterFor(src1))) {
            if (dest.GetVariableType() == VariableType.Double) {
                X86MoveFPInstruction mv = new X86MoveFPInstruction(true, opDest, op1);
                mv.AddNumOfSpaceForPrefix(4);
                out.add(mv);
            } else {
                X86MoveInstruction mv = new X86MoveInstruction(opDest, op1);
                mv.AddNumOfSpaceForPrefix(4);
                out.add(mv);
            }
        }

        // add and or sub based on the type
        if (dest.GetVariableType() == VariableType.Double) {
            Instruction inst = (type == AddType.Plus)
            ? new X86AddFPInstruction(true, opDest, op2)
            : new X86SubFPInstruction(true, opDest, op2);
            inst.AddNumOfSpaceForPrefix(4);
            out.add(inst);
        } else {
            Instruction inst = (type == AddType.Plus)
            ? new X86AddInstruction(opDest, op2)
            : new X86SubInstruction(opDest, op2);
            inst.AddNumOfSpaceForPrefix(4);
            out.add(inst);
        }

        // spill
        codeGenHelper.spillIfNeeded(dest, opDest, out);

        codeGenHelper.finishStep();
        return out;
    }

}
