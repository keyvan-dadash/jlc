package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;

/**
 * X86ExternInstruction adds extern to the final assembly for accessing external functions.
 * Prints “.extern <symbol>” .
 */
public class X86ExternInstruction implements Instruction {
    private final String symbol;
    private int numOfSpace;

    public X86ExternInstruction(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        String indent = " ".repeat(numOfSpace);
        return indent + "extern " + symbol;
    }
}