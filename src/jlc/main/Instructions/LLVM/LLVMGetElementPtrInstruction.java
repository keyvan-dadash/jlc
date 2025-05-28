package jlc.main.Instructions.LLVM;

import jlc.main.Instructions.Instruction;
import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableType;

public class LLVMGetElementPtrInstruction implements Instruction {
    private int numOfSpace;
    private Variable basePtr;      
    private Variable index;        
    private Variable result;       
    private VariableType elemType; 

    public LLVMGetElementPtrInstruction(Variable basePtr, Variable index, Variable result, VariableType elemType) {
        this.basePtr = basePtr;
        this.index = index;
        this.result = result;
        this.elemType = elemType;
        AddNumOfSpaceForPrefix(4);
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        // Determine the LLVM type for the element
        String llvmElemType = Utils.VariableTypeToLLVMVariableType(elemType);

        // For this instruction, assume basePtr is already <elemType>*
        return String.format("%s%s = getelementptr %s, %s* %s, i32 %s",
            Utils.GetNumOfSpace(this.numOfSpace),
            Utils.VariableToLLVMVariable(result),
            llvmElemType,
            llvmElemType,
            Utils.VariableToLLVMVariable(basePtr),
            Utils.VariableToLLVMVariable(index)
        );
    }
}