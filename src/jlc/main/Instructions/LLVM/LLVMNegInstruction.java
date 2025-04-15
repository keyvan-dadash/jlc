package jlc.main.Instructions.LLVM;

import jlc.main.Instructions.Instruction;
import jlc.main.Variables.Variable;

public class LLVMNegInstruction implements Instruction {
    
    private Variable operand;
    private Variable result;
    private int numOfSpace;
    
    public LLVMNegInstruction(Variable operand, Variable result) {
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
        switch (operand.GetVariableType()) {
            case Int:
                return String.format("%s%s = sub i32 0, %s",
                        Utils.GetNumOfSpace(this.numOfSpace),
                        Utils.VariableToLLVMVariable(result),
                        Utils.VariableToLLVMVariable(operand));
            case Double:
                return String.format("%s%s = fsub double 0.0, %s",
                        Utils.GetNumOfSpace(this.numOfSpace),
                        Utils.VariableToLLVMVariable(result),
                        Utils.VariableToLLVMVariable(operand));
            default:
                throw new RuntimeException("Neg instruction: unsupported type " + operand.GetVariableType());
        }
    }
}
