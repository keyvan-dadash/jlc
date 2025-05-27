package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 imul instruction: dest = dest * src
 * 
 * Syntax: imul dest, src
 * Illegal: dest must be a register.
 */
public class X86MulInstruction implements Instruction {
    private Operand dest;
    private Operand src;
    private int numOfSpace;

    public X86MulInstruction() {}

    public X86MulInstruction(Operand dest, Operand src) {
        setOperands(dest, src);
    }

    /**
     * @throws IllegalArgumentException if dest is memory.
     */
    public void setOperands(Operand dest, Operand src) {
        if (dest.isMemory()) {
            throw new IllegalArgumentException("imul: destination must be a register");
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
            throw new IllegalStateException("X86MulInstruction operands not initialized");
        }
        String indent = " ".repeat(numOfSpace);
        return String.format("%simul %s, %s", indent, dest, src);
    }
}