package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 subtract‐immediate instruction:
 *    sub dest, imm
 *
 * Illegal: dest and imm both memory (imm must be a constant).
 */
public class X86SubImmediateInstruction implements Instruction {
    private Operand dest;
    private int immediate;
    private int numOfSpace;

    /** Empty ctor; call setOperands() before GenerateInstruction(). */
    public X86SubImmediateInstruction() {}

    /**
     * @param dest destination operand (register or memory)
     * @param imm  immediate constant to subtract
     */
    public X86SubImmediateInstruction(Operand dest, int imm) {
        setOperands(dest, imm);
    }

    /**
     * Set or reset the operands.
     * @param dest destination register or memory
     * @param imm  immediate constant
     */
    public void setOperands(Operand dest, int imm) {
        this.dest = dest;
        this.immediate = imm;
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        if (dest == null) {
            throw new IllegalStateException("X86SubImmediateInstruction operands not initialized");
        }
        String indent = " ".repeat(numOfSpace);
        return String.format("%ssub %s, %d", indent, dest, immediate);
    }
}
