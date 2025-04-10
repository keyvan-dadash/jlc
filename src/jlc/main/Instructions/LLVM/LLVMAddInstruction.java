package jlc.main.Instructions.LLVM;

import jlc.main.Instructions.Instruction;
import jlc.main.Operations.AddType;
import jlc.main.Variables.Variable;

public class LLVMAddInstruction implements Instruction {

    private AddType addType;
    private Variable var1;
    private Variable var2;
    private Variable result;
    private int numOfSpace;

    public LLVMAddInstruction(AddType addType) {
        this.addType = addType;
    }

    public LLVMAddInstruction(AddType addType, Variable var1, Variable var2, Variable result) {
        this.addType = addType;
        this.SetVariables(var1, var2, result);
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
        switch (addType) {
            case Plus: {
                return generateInstructionForPlus();
            }
            case Minus: {
                return generateInstructionForMinus();
            }
            default: {
                throw new RuntimeException("unkown add operation has been give");
            }
        }
    }

    private String generateInstructionForPlus() {
        switch (var1.GetVariableType()) {
            case Int: {
                return String.format("%s%s = add i32 %s, %s",
                    Utils.GetNumOfSpace(this.numOfSpace),
                    Utils.VariableToLLVMVariable(result),
                    Utils.VariableToLLVMVariable(var1),
                    Utils.VariableToLLVMVariable(var2));
            }
            case Double: {
                return String.format("%s%s = fadd double %s, %s",
                    Utils.GetNumOfSpace(this.numOfSpace),
                    Utils.VariableToLLVMVariable(result),
                    Utils.VariableToLLVMVariable(var1),
                    Utils.VariableToLLVMVariable(var2));
            }
            default: {
                throw new RuntimeException("plus instruction is only possible for int and double");
            }
        }
    }

    private String generateInstructionForMinus() {
        switch (var1.GetVariableType()) {
            case Int: {
                return String.format("%s%s = sub i32 %s, %s",
                    Utils.GetNumOfSpace(this.numOfSpace),
                    Utils.VariableToLLVMVariable(result),
                    Utils.VariableToLLVMVariable(var1),
                    Utils.VariableToLLVMVariable(var2));
            }
            case Double: {
                return String.format("%s%s = fsub double %s, %s",
                    Utils.GetNumOfSpace(this.numOfSpace), 
                    Utils.VariableToLLVMVariable(result),
                    Utils.VariableToLLVMVariable(var1),
                    Utils.VariableToLLVMVariable(var2));
            }
            default: {
                throw new RuntimeException("minus instruction is only possible for int and double");
            }
        }
    }
}
