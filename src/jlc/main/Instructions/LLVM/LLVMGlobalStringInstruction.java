package jlc.main.Instructions.LLVM;

import jlc.main.Instructions.Instruction;

public class LLVMGlobalStringInstruction implements Instruction {
    private String globalName;
    private String stringValue;
    private int numOfSpace;

    public LLVMGlobalStringInstruction(String globalName, String stringValue) {
        this.globalName = globalName;
        this.stringValue = stringValue;
    }
    
    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }
    
    @Override
    public String GenerateInstruction() {
        int arrayLength = stringValue.length() + 1;
        
        String escapedString = stringEscape(stringValue);
        
        return String.format("%s@%s = global [%d x i8] c\"%s\\00\"",
                Utils.GetNumOfSpace(this.numOfSpace),
                globalName,
                arrayLength,
                escapedString);
    }
    
    private String stringEscape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
