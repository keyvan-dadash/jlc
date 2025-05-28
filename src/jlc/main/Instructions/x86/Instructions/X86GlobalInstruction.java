package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;

/**X86GlobalInstruction add global symbols
 * Prints “.globl <symbol>”
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