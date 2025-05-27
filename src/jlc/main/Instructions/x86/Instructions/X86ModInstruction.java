package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 mod instruction performs mod instructions.
 * 
 * Syntax (IR): mod dest, src
 */
public class X86ModInstruction implements Instruction {
    private Operand dest;
    private Operand src;
    private int numOfSpace;

    public X86ModInstruction() {}

    public X86ModInstruction(Operand dest, Operand src) {
        setOperands(dest, src);
    }

    public void setOperands(Operand dest, Operand src) {
        if (dest.isMemory()) {
            throw new IllegalArgumentException("mod: destination must be a register");
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
            throw new IllegalStateException("X86ModInstruction operands not initialized");
        }
        String indent = " ".repeat(numOfSpace);
        return String.format("%smod %s, %s", indent, dest, src);
    }
}