package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;

/**
 * Prints “.globl <symbol>” in the finalAsm.
 */
public class X86GlobalInstruction implements Instruction {
    private final String symbol;
    private int numOfSpace;

    public X86GlobalInstruction(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        String indent = " ".repeat(numOfSpace);
        return indent + "global " + symbol;
    }
}