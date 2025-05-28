package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;

public class X86CqoInstruction implements Instruction {
    private int numOfSpace;

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        String indent = " ".repeat(numOfSpace);
        return indent + "cqo";
    }
}
