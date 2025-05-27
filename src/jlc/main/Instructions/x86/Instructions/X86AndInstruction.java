package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 and instruction: dest = dest & src
 * 
 * Syntax: and dest, src
 * Illegal: memory-to-memory.
 */
public class X86AndInstruction implements Instruction {
    private Operand dest;
    private Operand src;
    private int numOfSpace;

    public X86AndInstruction() {}

    public X86AndInstruction(Operand dest, Operand src) {
        setOperands(dest, src);
    }

    /**
     * @throws IllegalArgumentException if both dest and src are memory.
     */
    public void setOperands(Operand dest, Operand src) {
        if (dest.isMemory() && src.isMemory()) {
            throw new IllegalArgumentException("and: cannot AND memory to memory");
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
            throw new IllegalStateException("X86AndInstruction operands not initialized");
        }
        String indent = " ".repeat(numOfSpace);
        return String.format("%sand %s, %s", indent, dest, src);
    }
}