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
 *     dest = LOAD [address]
 *
 * Here address is treated as the memory address (a Variable whose value is an address),
 * and dest is the register (or variable) receiving the loaded value.
 */
public class LoadIR implements IR {
    private Variable dest;    // the variable into which to load
    private Variable address; // the variable holding the memory address

    /**
     * Construct an uninitialized LoadIR.
     * You must call setOperands(...) before calling GetIR().
     */
    public LoadIR() {}

    /**
     * Construct a LoadIR with both operands set.
     *
     * @param dest    the variable to receive the loaded value
     * @param address the variable representing the memory address
     */
    public LoadIR(Variable dest, Variable address) {
        setOperands(dest, address);
    }

    /**
     * Set or reset the operands for this load instruction.
     *
     * @param dest    the variable to receive the loaded value
     * @param address the variable representing the memory address
     */
    public void setOperands(Variable dest, Variable address) {
        this.dest = dest;
        this.address = address;
    }

    @Override
    public String GetIR() {
        if (dest == null || address == null) {
            throw new IllegalStateException("LoadIR operands not fully initialized");
        }
        // Format: LOAD dest, [address]
        return String.format("LOAD %s, %s",
                dest.GetVariableName(),
                address.GetVariableName());
    }

    @Override
    public void PerformLivenessAnalysis(LivenessAnalysis livenessAnalysis) {
        // record use of the address variable
        if (address != null && Utils.isVirtualVariable(address)) {
            livenessAnalysis.recordVar(address);
        }
        // record definition of the dest variable
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

        // 1) Determine how to address the memory location
        Operand memOp;
        if (Utils.isGlobalVariable(address)) {
            // Global variable → access with RIP-relative addressing
            memOp = Operand.ofGlobal(address.GetVariableName(), address.GetVariableType());
        } else if (address.GetVariableKind() == VariableKind.ConstantVariable) {
            // Here is not address but contant variable
            memOp = Operand.ofImmediate(address);
        } else {
            // Stack-based variable (e.g., [rbp - X])
            memOp = Operand.ofMemory(address);
        }

        // 2) Determine destination operand (real register or scratch)
        Register rd = codeGenHelper.getRegisterFor(dest);
        Operand destOp;
        if (rd != null) {
            destOp = Operand.of(rd);
        } else if (dest.GetVariableType() == VariableType.Double) {
            destOp = Operand.of(Register.xmmScratch());
        } else {
            destOp = Operand.of(Register.gpScratch());
        }

        // 3) Emit the appropriate move instruction
        if (dest.GetVariableType() == VariableType.Double) {
            X86MoveFPInstruction mv = new X86MoveFPInstruction(
                /* isDouble = */ true,  // false = movss
                destOp, memOp
            );
            mv.AddNumOfSpaceForPrefix(4);
            out.add(mv);
        } else {
            X86MoveInstruction mv = new X86MoveInstruction(destOp, memOp);
            mv.AddNumOfSpaceForPrefix(4);
            out.add(mv);
        }

        // 4) Spill if necessary
        codeGenHelper.spillIfNeeded(dest, destOp, out);

        // 5) Finalize the step
        codeGenHelper.finishStep();
        return out;
    }
}
