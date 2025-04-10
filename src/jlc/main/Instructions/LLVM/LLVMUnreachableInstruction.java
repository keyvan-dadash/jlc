package jlc.main.Instructions.LLVM;

import jlc.main.Instructions.Instruction;

public class LLVMUnreachableInstruction implements Instruction {
    
    private int numOfSpace;

    public LLVMUnreachableInstruction() {
        // No additional initialization needed.
    }
    
    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }
    
    @Override
    public String GenerateInstruction() {
        // Generate a line with indentation followed by the "unreachable" terminator.
        return String.format("%sunreachable", Utils.GetNumOfSpace(this.numOfSpace));
    }
}