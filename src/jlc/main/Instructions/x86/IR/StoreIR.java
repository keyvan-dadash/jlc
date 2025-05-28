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
 *     move [address], reg
 *
 */
public class StoreIR implements IR {
    private Variable address;
    private Variable src;

    public StoreIR() {}

    public StoreIR(Variable address, Variable src) {
        setOperands(address, src);
    }

    public void setOperands(Variable address, Variable src) {
        this.address = address;
        this.src = src;
    }

    @Override
    public String GetIR() {
        if (address == null || src == null) {
            throw new IllegalStateException("StoreIR operands not fully initialized");
        }

        return String.format("STORE %s, %s",
                address.GetVariableName(),
                src.GetVariableName());
    }

    @Override
    public void PerformLivenessAnalysis(LivenessAnalysis livenessAnalysis) {
        if (address != null && Utils.isVirtualVariable(address)) {
            livenessAnalysis.recordVar(address);
        }
        if (src != null && Utils.isVirtualVariable(src)) {
            livenessAnalysis.recordVar(src);
        }

        livenessAnalysis.finishStep();
    }

    @Override
    public List<Instruction> GenerateX86Code(CodeGenHelper codeGenHelper) {
        List<Instruction> out = new ArrayList<>();

        codeGenHelper.spillCurrentStep(out);

        Operand opSrc = codeGenHelper.ensureInRegister(src, out);

        Operand opMem = Operand.ofMemory(address);

        if (src.GetVariableType() == VariableType.Double) {
            // movss [addr], xmmScratch or xmmReg
            // System.out.printf("%s %s\n", src.GetVariableName(), address.GetVariableName());
            X86MoveFPInstruction stfp = new X86MoveFPInstruction(
                true,
                opMem,
                opSrc
            );
            stfp.AddNumOfSpaceForPrefix(4);
            out.add(stfp);
        } else {
            // mov [addr], regScratch or reg
            X86MoveInstruction st = new X86MoveInstruction(opMem, opSrc);
            st.AddNumOfSpaceForPrefix(4);
            out.add(st);
        }

        codeGenHelper.finishStep();
        return out;
    }

}
