package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 movz instruction: move src to dest but with zero extended
 * 
 * Syntax:    movzx dest, src
 */
public class X86MovzxInstruction implements Instruction {
    private Operand dest;
    private Operand src;
    private int numOfSpace;

    public X86MovzxInstruction() {}

    public X86MovzxInstruction(Operand dest, Operand src) {
        setOperands(dest, src);
    }

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
