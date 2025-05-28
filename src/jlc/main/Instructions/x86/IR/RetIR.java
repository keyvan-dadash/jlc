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
import jlc.main.Instructions.x86.RegisterSaver;
import jlc.main.Instructions.x86.Utils;
import jlc.main.Instructions.x86.Instructions.X86AddImmediateInstruction;
import jlc.main.Instructions.x86.Instructions.X86MoveFPInstruction;
import jlc.main.Instructions.x86.Instructions.X86MoveInstruction;
import jlc.main.Instructions.x86.Instructions.X86PopInstruction;
import jlc.main.Instructions.x86.Instructions.X86RetInstruction;

/**
 * Represents a return instruction in the x86 IR.
 * RetIR will move the result in the RAX register.
 */
public class RetIR implements IR {
    private final Variable src;
    private final String func_name;

    public RetIR(String func_name) {
        this.func_name = func_name;
        this.src = null;
    }

    public RetIR(String func_name, Variable src) {
        this.func_name = func_name;
        this.src = src;
    }

    @Override
    public String GetIR() {
        if (src == null) {
            return "RET";
        } else {
            return String.format("RET %s", src.GetVariableName());
        }
    }

    @Override
    public void PerformLivenessAnalysis(LivenessAnalysis livenessAnalysis) {
        if (src != null && Utils.isVirtualVariable(src)) {
            livenessAnalysis.recordVar(src);
            // save result in xmm0 or rax based on the type of variable
            if (src.GetVariableType() == VariableType.Double) {
                livenessAnalysis.setFixedRegister(src, Register.XMM0);
            } else {
                livenessAnalysis.setFixedRegister(src, Register.RAX);
            }
        }
        // Advance to next instruction index
        livenessAnalysis.finishStep();
    }

    @Override
    public List<Instruction> GenerateX86Code(CodeGenHelper codeGenHelper) {
        List<Instruction> out = new ArrayList<>();

        codeGenHelper.spillCurrentStep(out);

        if (src != null) {
            Operand srcOp = codeGenHelper.ensureInRegister(src, out);

            Register retReg = (src.GetVariableType() == VariableType.Double)
                ? Register.XMM0
                : Register.RAX;
            Operand destOp = Operand.of(retReg);

            if (src.GetVariableType() == VariableType.Double) {
                X86MoveFPInstruction mvfp = new X86MoveFPInstruction(
                    true,
                    destOp, srcOp
                );
                mvfp.AddNumOfSpaceForPrefix(4);
                out.add(mvfp);
            } else {
                X86MoveInstruction mv = new X86MoveInstruction(
                    destOp, srcOp
                );
                mv.AddNumOfSpaceForPrefix(4);
                out.add(mv);
            }

            codeGenHelper.spillIfNeeded(src, destOp, out);
        }

        // Restore callee save registers
        if (func_name != "main") {
            // System.out.println(func_name);
            RegisterSaver.restore(codeGenHelper, out, Register.calleeSave(), codeGenHelper.getFunctionRegisters(func_name));
        }
        
        int frameSize = codeGenHelper.getFuncFrames().getOrDefault(func_name, 0);
        if (frameSize > 0) {
            X86AddImmediateInstruction addFrame = new X86AddImmediateInstruction(
                Operand.of(Register.RSP), frameSize
            );
            addFrame.AddNumOfSpaceForPrefix(4);
            out.add(addFrame);
        }

        X86PopInstruction x86PopInstruction = new X86PopInstruction(Operand.of(Register.RBP));
        x86PopInstruction.AddNumOfSpaceForPrefix(4);
        out.add(x86PopInstruction);

        X86RetInstruction retInst = new X86RetInstruction();
        retInst.AddNumOfSpaceForPrefix(4);
        out.add(retInst);

        codeGenHelper.finishStep();
        return out;
    }
}
