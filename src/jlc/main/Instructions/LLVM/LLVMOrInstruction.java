package jlc.main.Instructions.LLVM;

import jlc.main.Instructions.Instruction;
import jlc.main.Variables.Variable;

public class LLVMOrInstruction implements Instruction {
    private Variable var1;
    private Variable var2;
    private Variable result;
    private int numOfSpace;

    public LLVMOrInstruction() {
    }

    public LLVMOrInstruction(Variable var1, Variable var2, Variable result) {
        this.SetVariables(var1, var2, result);
    }

    public void SetVariables(Variable var1, Variable var2, Variable result) {
        this.var1 = var1;
        this.var2 = var2;
        this.result = result;
        AddNumOfSpaceForPrefix(4);
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        return String.format("%s%s = or %s %s, %s",
                Utils.GetNumOfSpace(this.numOfSpace),
                Utils.VariableToLLVMVariable(result),
                Utils.VariableTypeToLLVMVariableType(result.GetVariableType()),
                Utils.VariableToLLVMVariable(var1),
                Utils.VariableToLLVMVariable(var2));
    }
}
