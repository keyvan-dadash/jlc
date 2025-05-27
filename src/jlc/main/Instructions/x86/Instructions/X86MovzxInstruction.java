package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 MOVZX instruction: zero-extend byte → register.
 *
 * Syntax:    movzx dest, src
 *   - dest must be a register (16- or 32-bit)
 *   - src may be a register (8-bit) or memory
 */
public class X86MovzxInstruction implements Instruction {
    private Operand dest;
    private Operand src;
    private int numOfSpace;

    /** Empty ctor; must call setOperands() before GenerateInstruction(). */
    public X86MovzxInstruction() {}

    /**
     * @param dest destination register (16- or 32-bit)
     * @param src  source operand (8-bit register or memory)
     */
    public X86MovzxInstruction(Operand dest, Operand src) {
        setOperands(dest, src);
    }

    /**
     * Set or reset the operands.
     * @throws IllegalArgumentException if dest is memory or both dest/src are memory
     */
    public void setOperands(Operand dest, Operand src) {
        if (dest.isMemory()) {
            throw new IllegalArgumentException("movzx: destination must be a register");
        }
        if (dest.isMemory() && src.isMemory()) {
            throw new IllegalArgumentException("movzx: cannot movzx memory to memory");
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
            throw new IllegalStateException("X86MovzxInstruction operands not initialized");
        }
        String indent = " ".repeat(numOfSpace);
        return String.format("%smovzx %s, %s", indent, dest, src);
    }
}
