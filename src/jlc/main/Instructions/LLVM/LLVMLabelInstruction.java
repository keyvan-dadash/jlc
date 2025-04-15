package jlc.main.Instructions.LLVM;

import jlc.main.Instructions.Instruction;

public class LLVMLabelInstruction implements Instruction {
    private int numOfSpace;
    String labelName;

    public LLVMLabelInstruction(String labelName) {
        this.labelName = labelName;
        AddNumOfSpaceForPrefix(0);
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        return String.format("%s%s:", Utils.GetNumOfSpace(this.numOfSpace), labelName);
    }
}
