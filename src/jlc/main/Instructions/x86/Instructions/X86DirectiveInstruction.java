package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;

/**
 * Emits any raw assembler directive or label verbatim.
 */
public class X86DirectiveInstruction implements Instruction {
    private final String text;
    private int numOfSpace;

    public X86DirectiveInstruction(String text) {
        this.text = text;
    }
    @Override public void AddNumOfSpaceForPrefix(int n) { this.numOfSpace = n; }
    @Override public String GenerateInstruction() {
        String indent = " ".repeat(numOfSpace);
        return indent + text;
    }
}
