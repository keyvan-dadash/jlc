package jlc.main.Instructions.LLVM;

import jlc.main.Instructions.Instruction;
import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableType;

public class LLVMNotInstruction implements Instruction {
    
    private Variable operand;
    private Variable result;
    private int numOfSpace;
    
    public LLVMNotInstruction(Variable operand, Variable result) {
        this.operand = operand;
        this.result = result;
        AddNumOfSpaceForPrefix(4);
    }
    
    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }
    
    @Override
    public String GenerateInstruction() {
        if (operand.GetVariableType() != VariableType.Boolean) {
            throw new RuntimeException("Not instruction can only be applied to boolean variables");
        }
        // Use XOR for booleans: result = xor i1 operand, true
        return String.format("%s%s = xor i1 %s, true",
                Utils.GetNumOfSpace(this.numOfSpace),
                Utils.VariableToLLVMVariable(result),
                Utils.VariableToLLVMVariable(operand));
    }
}
