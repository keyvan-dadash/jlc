package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;

/**
 * x86 call instruction:
 *   call funcName
 */
public class X86CallInstruction implements Instruction {
    private String function;
    private int numOfSpace;

    public X86CallInstruction() {}

    public X86CallInstruction(String function) {
        this.function = function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        if (function == null || function.isEmpty()) {
            throw new IllegalStateException("X86CallInstruction: function not specified");
        }
        String indent = " ".repeat(numOfSpace);
        return String.format("%scall %s", indent, function);
    }
}