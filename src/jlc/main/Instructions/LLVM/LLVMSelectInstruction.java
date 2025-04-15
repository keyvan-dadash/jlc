package jlc.main.Instructions.LLVM;

import jlc.main.Instructions.Instruction;
import jlc.main.Variables.Variable;

public class LLVMSelectInstruction implements Instruction {
    
    private Variable condition;
    private Variable trueValue;
    private Variable falseValue;
    private Variable result;
    private int numOfSpace;
    
    public LLVMSelectInstruction(Variable condition, Variable trueValue, Variable falseValue, Variable result) {
        this.condition = condition;
        this.trueValue = trueValue;
        this.falseValue = falseValue;
        this.result = result;
        AddNumOfSpaceForPrefix(4);
    }
    
    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }
    
    @Override
    public String GenerateInstruction() {
        String condTypeStr = Utils.VariableTypeToLLVMVariableType(condition.GetVariableType());
        String valueTypeStr = Utils.VariableTypeToLLVMVariableType(trueValue.GetVariableType());
        return String.format("%s%s = select %s %s, %s %s, %s %s",
                Utils.GetNumOfSpace(this.numOfSpace),
                Utils.VariableToLLVMVariable(result),
                condTypeStr,
                Utils.VariableToLLVMVariable(condition),
                valueTypeStr,
                Utils.VariableToLLVMVariable(trueValue),
                valueTypeStr,
                Utils.VariableToLLVMVariable(falseValue)
        );
    }
}
