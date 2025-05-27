package jlc.main.Instructions.x86.IR;

import jlc.main.Variables.Variable;
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
 * Represents a register‐to‐memory store in the x86 IR:
 *     STORE [address], src
 *
 * Here, `address` is a Variable whose value is a memory address,
 * and `src` is the Variable whose value will be written into that memory.
 */
public class StoreIR implements IR {
    private Variable address;  // the memory address target
    private Variable src;      // the value to store

    /**
     * Construct an uninitialized StoreIR.
     * You must call setOperands(...) before calling GetIR().
     */
    public StoreIR() {}

    /**
     * Construct a StoreIR with both operands.
     *
     * @param address the variable representing the memory address
     * @param src     the variable whose value will be stored
     */
    public StoreIR(Variable address, Variable src) {
        setOperands(address, src);
    }

    /**
     * Set or reset the operands for this store instruction.
     *
     * @param address the variable holding the memory address
     * @param src     the variable whose value to store
     */
    public void setOperands(Variable address, Variable src) {
        this.address = address;
        this.src = src;
    }

    @Override
    public String GetIR() {
        if (address == null || src == null) {
            throw new IllegalStateException("StoreIR operands not fully initialized");
        }
        // Format: STORE [address], src
        return String.format("STORE %s, %s",
                address.GetVariableName(),
                src.GetVariableName());
    }

    @Override
    public void PerformLivenessAnalysis(LivenessAnalysis livenessAnalysis) {
        // record uses of both address and src
        if (address != null && Utils.isVirtualVariable(address)) {
            livenessAnalysis.recordVar(address);
        }
        if (src != null && Utils.isVirtualVariable(src)) {
            livenessAnalysis.recordVar(src);
        }
        // advance to next instruction index
        livenessAnalysis.finishStep();
    }

    @Override
    public List<Instruction> GenerateX86Code(CodeGenHelper codeGenHelper) {
        List<Instruction> out = new ArrayList<>();

        codeGenHelper.spillCurrentStep(out);

        // 1) Load src into a register or scratch if it was spilled
        Operand opSrc = codeGenHelper.ensureInRegister(src, out);

        // 2) The address variable is already “[ebp-…]”, so build a memory operand directly
        Operand opMem = Operand.ofMemory(address);

        // 3) Emit the store using src’s type
        if (src.GetVariableType() == VariableType.Double) {
            // movss [addr], xmmScratch or xmmReg
            // System.out.printf("%s %s\n", src.GetVariableName(), address.GetVariableName());
            X86MoveFPInstruction stfp = new X86MoveFPInstruction(
                /* isDouble = */ true,
                /* dest = */ opMem,
                /* src  = */ opSrc
            );
            stfp.AddNumOfSpaceForPrefix(4);
            out.add(stfp);
        } else {
            // mov [addr], regScratch or reg
            X86MoveInstruction st = new X86MoveInstruction(opMem, opSrc);
            st.AddNumOfSpaceForPrefix(4);
            out.add(st);
        }

        // 4) Advance the IR step
        codeGenHelper.finishStep();
        return out;
    }

}
