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
 * Two forms:
 *   - Void return:    RET
 *   - Value return:   RET src
 */
public class RetIR implements IR {
    private final Variable src;  // if null, this is a void return
    private final String func_name;  // if null, this is a void return

    /** Void return. */
    public RetIR(String func_name) {
        this.func_name = func_name;
        this.src = null;
    }

    /**
     * Return with a value.
     * @param src the variable whose value to return
     */
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
        // If returning a value, record its use and fix it to the return register
        if (src != null && Utils.isVirtualVariable(src)) {
            livenessAnalysis.recordVar(src);
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
            // 1) materialize src into its register or scratch
            Operand srcOp = codeGenHelper.ensureInRegister(src, out);

            // 2) pick the fixed return register
            Register retReg = (src.GetVariableType() == VariableType.Double)
                ? Register.XMM0
                : Register.RAX;
            Operand destOp = Operand.of(retReg);

            // 3) move src → return register, int vs. “double”
            if (src.GetVariableType() == VariableType.Double) {
                // movss xmm0, srcOp
                X86MoveFPInstruction mvfp = new X86MoveFPInstruction(
                    /* isDouble= */ true,
                    destOp, srcOp
                );
                mvfp.AddNumOfSpaceForPrefix(4);
                out.add(mvfp);
            } else {
                // mov rax, srcOp
                X86MoveInstruction mv = new X86MoveInstruction(
                    destOp, srcOp
                );
                mv.AddNumOfSpaceForPrefix(4);
                out.add(mv);
            }

            // 4) if for some reason dest was "spilled" (shouldn't happen due to fixed),
            //    spill back to memory
            codeGenHelper.spillIfNeeded(src, destOp, out);
        }

        // Restore callee save registers
        if (func_name != "main") {
            // System.out.println(func_name);
            RegisterSaver.restore(codeGenHelper, out, Register.calleeSave(), codeGenHelper.getFunctionRegisters(func_name));
        }
        
        // — epilogue: undo our own frame allocation —
        int frameSize = codeGenHelper.getFuncFrames().getOrDefault(func_name, 0);
        if (frameSize > 0) {
            X86AddImmediateInstruction addFrame = new X86AddImmediateInstruction(
                Operand.of(Register.RSP), frameSize
            );
            addFrame.AddNumOfSpaceForPrefix(4);
            out.add(addFrame);
        }

        // pop rbp
        X86PopInstruction x86PopInstruction = new X86PopInstruction(Operand.of(Register.RBP));
        x86PopInstruction.AddNumOfSpaceForPrefix(4);
        out.add(x86PopInstruction);

        // 5) emit the actual RET
        X86RetInstruction retInst = new X86RetInstruction();
        retInst.AddNumOfSpaceForPrefix(4);
        out.add(retInst);

        // 6) advance the helper’s instruction counter
        codeGenHelper.finishStep();
        return out;
    }
}
