package jlc.main.Instructions.LLVM;

import jlc.main.Instructions.Instruction;
import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableType;

public class LLVMAllocaInstruction implements Instruction {
    
    private Variable result;
    private VariableType allocatedType;
    private int numOfSpace;
    
    public LLVMAllocaInstruction(Variable result, VariableType allocatedType) {
        this.result = result;
        this.allocatedType = allocatedType;
        AddNumOfSpaceForPrefix(4);
    }
    
    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }
    
    @Override
    public String GenerateInstruction() {
        return String.format("%s%s = alloca %s",
                Utils.GetNumOfSpace(this.numOfSpace),
                Utils.VariableToLLVMVariable(result),
                Utils.VariableTypeToLLVMVariableType(allocatedType));
    }
}
