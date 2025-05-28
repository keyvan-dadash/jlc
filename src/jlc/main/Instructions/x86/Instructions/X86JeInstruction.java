package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;

/**
 * x86 je instruction:
 *   je label
 */
public class X86JeInstruction implements Instruction {
    private String label;
    private int numOfSpace;

    public X86JeInstruction() {}

    public X86JeInstruction(String label) {
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
            throw new IllegalStateException("X86JeInstruction: label not specified");
        }
        String indent = " ".repeat(numOfSpace);
        return String.format("%sje %s", indent, label);
    }
}