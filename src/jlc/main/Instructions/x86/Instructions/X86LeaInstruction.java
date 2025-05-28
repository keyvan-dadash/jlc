package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 lea instruction: load effective address.
 *
 * Syntax: lea dest, src
 */
public class X86LeaInstruction implements Instruction {
    private Operand dest;
    private Operand src;
    private int numOfSpace;

    public X86LeaInstruction() {}

    public X86LeaInstruction(Operand dest, Operand src) {
        setOperands(dest, src);
    }

    public void setOperands(Operand dest, Operand src) {
        if (!dest.isRegister()) {
            throw new IllegalArgumentException("lea: destination must be a register");
        }
        if (!src.isMemory()) {
            throw new IllegalArgumentException("lea: source must be a memory operand");
        }
        this.dest = dest;
        this.src  = src;
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        if (dest == null || src == null) {
            throw new IllegalStateException("X86LeaInstruction operands not initialized");
        }
        String indent = " ".repeat(numOfSpace);
        return String.format("%slea %s, %s", indent, dest, src);
    }
}
