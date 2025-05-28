package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 add instruction: 
 *  add dest, src
 *
 */
public class X86AddInstruction implements Instruction {
    private Operand dest;
    private Operand src;
    private int numOfSpace;

    public X86AddInstruction() {}

    public X86AddInstruction(Operand dest, Operand src) {
        setOperands(dest, src);
    }

    public void setOperands(Operand dest, Operand src) {
        if (dest.isMemory() && src.isMemory()) {
            throw new IllegalArgumentException("add: cannot add memory to memory");
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
            throw new IllegalStateException("X86AddInstruction operands not initialized");
        }
        String indent = " ".repeat(numOfSpace);
        return String.format("%sadd %s, %s", indent, dest, src);
    }
}