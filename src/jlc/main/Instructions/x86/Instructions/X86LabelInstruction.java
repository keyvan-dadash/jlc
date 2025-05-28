package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;

/**
 * x86 label instruction:
 *     label:
 */
public class X86LabelInstruction implements Instruction {
    private String label;
    private int numOfSpace;

    public X86LabelInstruction() {}

    public X86LabelInstruction(String label) {
        this.label = label;
    }

    /**
     * Set or change the label name (without the colon).
     */
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
            throw new IllegalStateException("X86LabelInstruction: label not specified");
        }
        String indent = " ".repeat(numOfSpace);
        return String.format("%s%s:", indent, label);
    }
}