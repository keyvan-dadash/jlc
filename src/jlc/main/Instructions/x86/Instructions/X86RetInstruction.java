package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;

/**
 * x86 ret instruction:
 *   ret
 */
public class X86RetInstruction implements Instruction {
    private int numOfSpace;

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        String indent = " ".repeat(numOfSpace);
        return indent + "ret";
    }
}