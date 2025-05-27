package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;

/**
 * A pseudo-instruction to switch assembler section.
 *   section .text
 *   section .data
 *   section .rodata
 */
public class X86SectionInstruction implements Instruction {
    private final String sectionName;
    private int numOfSpace;

    public X86SectionInstruction(String sectionName) {
        this.sectionName = sectionName;
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        String indent = " ".repeat(numOfSpace);
        return String.format("%ssection %s", indent, sectionName);
    }
}