package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;

/**
 * x86 jne instruction:
 *   jne label
 */
public class X86JneInstruction implements Instruction {
    private String label;
    private int numOfSpace;

    public X86JneInstruction() {}

    public X86JneInstruction(String label) {
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
            throw new IllegalStateException("X86JneInstruction: label not specified");
        }
        String indent = " ".repeat(numOfSpace);
        return String.format("%sjne %s", indent, label);
    }
}