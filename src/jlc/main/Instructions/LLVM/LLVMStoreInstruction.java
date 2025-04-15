package jlc.main.Instructions.LLVM;

import jlc.main.Instructions.Instruction;
import jlc.main.Variables.Variable;

public class LLVMStoreInstruction implements Instruction {
    private int numOfSpace;
    Variable from;
    Variable to;

    public LLVMStoreInstruction() {

    }

    public LLVMStoreInstruction(Variable from, Variable to) {
        this.SetVariables(from, to);
    }

    public void SetVariables(Variable from, Variable to) {
        this.from = from;
        this.to = to;
        AddNumOfSpaceForPrefix(4);
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        return String.format("%sstore %s %s, %s* %s", 
            Utils.GetNumOfSpace(this.numOfSpace),
            Utils.VariableTypeToLLVMVariableType(from.GetVariableType()),
            Utils.VariableToLLVMVariable(from),
            Utils.VariableTypeToLLVMVariableType(to.GetVariableType()),
            Utils.VariableToLLVMVariable(to));
    }
}
