package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;

/**
 * x86 call instruction:
 *   call funcName
 */
public class X86CallInstruction implements Instruction {
    private String function;
    private int numOfSpace;

    /** Uninitialized; must call setFunction(...) before GenerateInstruction(). */
    public X86CallInstruction() {}

    /** Construct a call to the given function name. */
    public X86CallInstruction(String function) {
        this.function = function;
    }

    /** Set or change the target function name. */
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