package jlc.main.Instructions.x86.IR;

import jlc.main.Operations.MulType;
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
import jlc.main.Instructions.x86.Instructions.X86CqoInstruction;
import jlc.main.Instructions.x86.Instructions.X86DivInstruction;
import jlc.main.Instructions.x86.Instructions.X86MoveFPInstruction;
import jlc.main.Instructions.x86.Instructions.X86DivFPInstruction;
import jlc.main.Instructions.x86.Instructions.X86MoveInstruction;
import jlc.main.Instructions.x86.Instructions.X86MulFPInstruction;
import jlc.main.Instructions.x86.Instructions.X86MulInstruction;

/**
 * Represents a three‐operand multiplication/division/modulo in the x86 IR:
 *   dest = src1 * src2    (Times)
 *   dest = src1 / src2    (Div)
 *   dest = src1 % src2    (Mod)
 *
 * This IR has two fixed registers RAX and RDX
 */
public class MulIR implements IR {
    private final MulType type;
    private Variable dest, src1, src2;

    public MulIR(MulType type) { this.type = type; }
    public MulIR(MulType type, Variable dest, Variable src1, Variable src2) {
        this.type = type;
        setOperands(dest, src1, src2);
    }
    public void setOperands(Variable d, Variable s1, Variable s2) {
        this.dest = d; this.src1 = s1; this.src2 = s2;
    }

    @Override
    public String GetIR() {
        if (dest==null||src1==null||src2==null) throw new IllegalStateException();
        return String.format("%s %s, %s, %s",
            type, dest.GetVariableName(),
            src1.GetVariableName(), src2.GetVariableName());
    }

    @Override
    public void PerformLivenessAnalysis(LivenessAnalysis la) {
        if (Utils.isVirtualVariable(src1)) la.recordVar(src1);
        if (Utils.isVirtualVariable(src2)) la.recordVar(src2);
        if (Utils.isVirtualVariable(dest)) la.recordVar(dest);

        if (dest.GetVariableType()==VariableType.Int &&
           (type==MulType.Div||type==MulType.Mod))
        {
            // dividend should be in RAX
            la.setFixedRegister(src1, Register.RAX);

            // divisor will be in RAX or mod will be in RDX
            la.setFixedRegister(dest, type==MulType.Div ? Register.RAX : Register.RDX);
        }

        la.finishStep();
    }

    @Override
    public List<Instruction> GenerateX86Code(CodeGenHelper helper) {
        List<Instruction> out = new ArrayList<>();

        helper.spillCurrentStep(out);

        Operand op1 = helper.ensureInRegister(src1, out);
        Operand op2 = helper.ensureInRegister(src2, out);

        Register rd = helper.getRegisterFor(dest);
        if (rd == null) {
            rd = dest.GetVariableType() == VariableType.Double
                ? Register.xmmScratch()
                : Register.gpScratch();
        }
        Operand opDest = Operand.of(rd);

        if (dest.GetVariableType() == VariableType.Int) {
            switch(type) {
            case Times:
                if (!opDest.equals(op1)) {
                    X86MoveInstruction mv = new X86MoveInstruction(opDest, op1);
                    mv.AddNumOfSpaceForPrefix(4);
                    out.add(mv);
                }
                X86MulInstruction mul = new X86MulInstruction(opDest, op2);
                mul.AddNumOfSpaceForPrefix(4);
                out.add(mul);
                break;

            case Div:
            case Mod:
                if (!op1.equals(Operand.of(Register.RAX))) {
                    X86MoveInstruction mvRax = new X86MoveInstruction(
                        Operand.of(Register.RAX), op1);
                    mvRax.AddNumOfSpaceForPrefix(4);
                    out.add(mvRax);
                }

                Operand idivOp = op2;
                if (!idivOp.isRegister() || idivOp.getRegister().equals(Register.RDX)) {
                    Register tmp = Register.gpScratch();
                    X86MoveInstruction mvTmp = new X86MoveInstruction(
                        Operand.of(tmp), idivOp);
                    mvTmp.AddNumOfSpaceForPrefix(4);
                    out.add(mvTmp);
                    idivOp = Operand.of(tmp);
                }

                X86CqoInstruction cqo = new X86CqoInstruction();
                cqo.AddNumOfSpaceForPrefix(4);
                out.add(cqo);

                X86DivInstruction idiv = new X86DivInstruction(idivOp);
                idiv.AddNumOfSpaceForPrefix(4);
                out.add(idiv);

                Register resultReg = (type == MulType.Div ? Register.RAX : Register.RDX);
                if (rd != resultReg) {
                    X86MoveInstruction mvRes = new X86MoveInstruction(
                        opDest, Operand.of(resultReg));
                    mvRes.AddNumOfSpaceForPrefix(4);
                    out.add(mvRes);
                }
                break;
            }

        } else {
            switch(type) {
            case Times:

                X86MoveFPInstruction mvf = new X86MoveFPInstruction(
                    true, opDest, op1
                );
                mvf.AddNumOfSpaceForPrefix(4);
                out.add(mvf);

                X86MulFPInstruction mfp = new X86MulFPInstruction(
                    true, opDest, op2
                );
                mfp.AddNumOfSpaceForPrefix(4);
                out.add(mfp);
                break;
    
            case Div:

                X86MoveFPInstruction mvfd = new X86MoveFPInstruction(
                    true, opDest, op1
                );
                mvfd.AddNumOfSpaceForPrefix(4);
                out.add(mvfd);

                X86DivFPInstruction dfp = new X86DivFPInstruction(
                    true, opDest, op2
                );
                dfp.AddNumOfSpaceForPrefix(4);
                out.add(dfp);
                break;
    
            default:
                throw new UnsupportedOperationException("FP MOD not supported");
            }
        }

        helper.spillIfNeeded(dest, opDest, out);

        helper.finishStep();
        return out;
    }

}
