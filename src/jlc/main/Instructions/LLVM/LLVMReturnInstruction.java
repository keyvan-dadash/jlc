package jlc.main.Instructions.LLVM;

import jlc.main.Instructions.Instruction;
import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableType;

public class LLVMReturnInstruction implements Instruction {
    
    private Variable returnVariable;
    private int numOfSpace;

    public LLVMReturnInstruction() {
        this.returnVariable = null;
    }

    public LLVMReturnInstruction(Variable returnVariable) {
        this.returnVariable = returnVariable;
    }
    
    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }
    
    @Override
    public String GenerateInstruction() {
        String prefix = Utils.GetNumOfSpace(this.numOfSpace);
        if (returnVariable == null) {
            return prefix + "ret void";
        } else {
            VariableType retType = returnVariable.GetVariableType();
            String llvmType = Utils.VariableTypeToLLVMVariableType(retType);
            String llvmVar = Utils.VariableToLLVMVariable(returnVariable);
            return String.format("%sret %s %s", prefix, llvmType, llvmVar);
        }
    }
}