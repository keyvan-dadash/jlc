package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;

/**
 * x86 jmp instruction:
 *   jmp label
 */
public class X86JmpInstruction implements Instruction {
    private String label;
    private int numOfSpace;

    public X86JmpInstruction() {}

    public X86JmpInstruction(String label) {
        this.label = label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        if (label == null || label.isEmpty()) {
            throw new IllegalStateException("X86JmpInstruction: label not specified");
        }
        String indent = " ".repeat(numOfSpace);
        return String.format("%sjmp %s", indent, label);
    }
}