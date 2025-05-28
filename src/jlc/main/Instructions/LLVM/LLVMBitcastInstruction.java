package jlc.main.Instructions.LLVM;

import jlc.main.Instructions.Instruction;
import jlc.main.Variables.Variable;

public class LLVMBitcastInstruction implements Instruction {
    private int numOfSpace;
    private Variable src;      
    private Variable dest;     
    private String destType;   

    public LLVMBitcastInstruction(Variable src, Variable dest, String destType) {
        this.src = src;
        this.dest = dest;
        this.destType = destType;
        AddNumOfSpaceForPrefix(4);
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        return String.format("%s%s = bitcast %s %s to %s",
            Utils.GetNumOfSpace(this.numOfSpace),
            Utils.VariableToLLVMVariable(dest),
            Utils.VariableTypeToLLVMVariableType(src.GetVariableType()),
            Utils.VariableToLLVMVariable(src),
            destType
        );
    }
}