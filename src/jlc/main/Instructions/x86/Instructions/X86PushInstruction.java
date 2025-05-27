package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 push instruction:
 *   push src
 */
public class X86PushInstruction implements Instruction {
    private Operand src;
    private int numOfSpace;

    /** Uninitialized; must call setOperand(...) before GenerateInstruction(). */
    public X86PushInstruction() {}

    /** Construct a push of the given operand (register or memory). */
    public X86PushInstruction(Operand src) {
        this.src = src;
    }

    /** Set or change the source operand. */
    public void setOperand(Operand src) {
        this.src = src;
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        if (src == null) {
            throw new IllegalStateException("X86PushInstruction: operand not specified");
        }
        String indent = " ".repeat(numOfSpace);
        return String.format("%spush %s", indent, src);
    }
}