package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 sub instruction: dest = dest - src
 * 
 * Syntax: sub dest, src
 * Illegal: memory-to-memory.
 */
public class X86SubInstruction implements Instruction {
    private Operand dest;
    private Operand src;
    private int numOfSpace;

    public X86SubInstruction() {}

    public X86SubInstruction(Operand dest, Operand src) {
        setOperands(dest, src);
    }

    /**
     * @throws IllegalArgumentException if both dest and src are memory.
     */
    public void setOperands(Operand dest, Operand src) {
        if (dest.isMemory() && src.isMemory()) {
            throw new IllegalArgumentException("sub: cannot subtract memory from memory");
        }
        this.dest = dest;
        this.src = src;
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        if (dest == null || src == null) {
            throw new IllegalStateException("X86SubInstruction operands not initialized");
        }
        String indent = " ".repeat(numOfSpace);
        return String.format("%ssub %s, %s", indent, dest, src);
    }
}