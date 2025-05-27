package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 or instruction: dest = dest | src
 * 
 * Syntax: or dest, src
 * Illegal: memory-to-memory.
 */
public class X86OrInstruction implements Instruction {
    private Operand dest;
    private Operand src;
    private int numOfSpace;

    public X86OrInstruction() {}

    public X86OrInstruction(Operand dest, Operand src) {
        setOperands(dest, src);
    }

    /**
     * @throws IllegalArgumentException if both dest and src are memory.
     */
    public void setOperands(Operand dest, Operand src) {
        if (dest.isMemory() && src.isMemory()) {
            throw new IllegalArgumentException("or: cannot OR memory to memory");
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
            throw new IllegalStateException("X86OrInstruction operands not initialized");
        }
        String indent = " ".repeat(numOfSpace);
        return String.format("%sor %s, %s", indent, dest, src);
    }
}