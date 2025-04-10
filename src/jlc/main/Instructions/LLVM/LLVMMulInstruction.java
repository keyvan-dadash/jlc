package jlc.main.Instructions.LLVM;

import jlc.main.Instructions.Instruction;
import jlc.main.Operations.MulType;
import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableType;

public class LLVMMulInstruction implements Instruction {
    
    private MulType mulType;
    private Variable var1;
    private Variable var2;
    private Variable result;
    private int numOfSpace;

    public LLVMMulInstruction(MulType mulType) {
        this.mulType = mulType;
    }
    
    public LLVMMulInstruction(MulType mulType, Variable var1, Variable var2, Variable result) {
        this.mulType = mulType;
        SetVariables(var1, var2, result);
    }
    
    public void SetVariables(Variable var1, Variable var2, Variable result) {
        this.var1 = var1;
        this.var2 = var2;
        this.result = result;
    }
    
    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }
    
    @Override
    public String GenerateInstruction() {
        switch (mulType) {
            case Times:
                return generateInstructionForTimes();
            case Div:
                return generateInstructionForDiv();
            case Mod:
                return generateInstructionForMod();
            default:
                throw new RuntimeException("Unknown mul operation provided");
        }
    }
    
    private String generateInstructionForTimes() {
        switch (var1.GetVariableType()) {
            case Int:
                return String.format("%s%s = mul i32 %s, %s",
                    Utils.GetNumOfSpace(this.numOfSpace),
                    Utils.VariableToLLVMVariable(result),
                    Utils.VariableToLLVMVariable(var1),
                    Utils.VariableToLLVMVariable(var2));
            case Double:
                return String.format("%s%s = fmul double %s, %s",
                    Utils.GetNumOfSpace(this.numOfSpace),
                    Utils.VariableToLLVMVariable(result),
                    Utils.VariableToLLVMVariable(var1),
                    Utils.VariableToLLVMVariable(var2));
            default:
                throw new RuntimeException("Times operation is only possible for int and double");
        }
    }
    
    private String generateInstructionForDiv() {
        switch (var1.GetVariableType()) {
            case Int:
                return String.format("%s%s = sdiv i32 %s, %s",
                    Utils.GetNumOfSpace(this.numOfSpace),
                    Utils.VariableToLLVMVariable(result),
                    Utils.VariableToLLVMVariable(var1),
                    Utils.VariableToLLVMVariable(var2));
            case Double:
                return String.format("%s%s = fdiv double %s, %s",
                    Utils.GetNumOfSpace(this.numOfSpace),
                    Utils.VariableToLLVMVariable(result),
                    Utils.VariableToLLVMVariable(var1),
                    Utils.VariableToLLVMVariable(var2));
            default:
                throw new RuntimeException("Div operation is only possible for int and double");
        }
    }
    
    private String generateInstructionForMod() {
        if (var1.GetVariableType() == VariableType.Int) {
            return String.format("%s%s = srem i32 %s, %s",
                Utils.GetNumOfSpace(this.numOfSpace),
                Utils.VariableToLLVMVariable(result),
                Utils.VariableToLLVMVariable(var1),
                Utils.VariableToLLVMVariable(var2));
        }
        throw new RuntimeException("Mod operation is only possible for int");
    }
}
