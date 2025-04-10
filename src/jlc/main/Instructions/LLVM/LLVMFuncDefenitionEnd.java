package jlc.main.Instructions.LLVM;

import jlc.main.Instructions.Instruction;

public class LLVMFuncDefenitionEnd implements Instruction {
    private int numOfSpace;

    public LLVMFuncDefenitionEnd() {

    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        return String.format("%s}\n", Utils.GetNumOfSpace(this.numOfSpace));
    }
}
