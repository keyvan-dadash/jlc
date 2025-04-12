package jlc.main.Instructions.LLVM;

import jlc.main.Instructions.Instruction;
import jlc.main.Variables.Variable;

public class LLVMLoadGlobalStringInstruction implements Instruction {
    
    private Variable target;
    private String globalName;
    private String stringLiteral;
    private int numOfSpace;
    
    public LLVMLoadGlobalStringInstruction(Variable target, String globalName, String stringLiteral) {
        this.target = target;
        this.globalName = globalName;
        this.stringLiteral = stringLiteral;
    }
    
    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }
    
    @Override
    public String GenerateInstruction() {
        int arrayLength = stringLiteral.length() + 1;
        return String.format("%s%s = getelementptr inbounds [%d x i8], [%d x i8]* @%s, i32 0, i32 0",
                Utils.GetNumOfSpace(this.numOfSpace),
                Utils.VariableToLLVMVariable(target),
                arrayLength, arrayLength,
                globalName);
    }
}
