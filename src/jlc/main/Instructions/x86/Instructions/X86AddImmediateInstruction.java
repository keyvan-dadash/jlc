package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 add immediate instruction:
 *    add dest, imm
 *
 * Only registers (or memory) may appear as dest; immediate is a 32-bit constant.
 */
public class X86AddImmediateInstruction implements Instruction {
    private Operand dest;
    private int immediate;
    private int numOfSpace;

    /** Empty ctor; must call setOperands() before GenerateInstruction(). */
    public X86AddImmediateInstruction() {}

    /**
     * Construct with destination and immediate.
     * @param dest   destination operand (register or memory)
     * @param immediate the constant to add
     */
    public X86AddImmediateInstruction(Operand dest, int immediate) {
        setOperands(dest, immediate);
    }

    /**
     * Set or reset the operands.
     * @throws IllegalArgumentException if dest is null
     */
    public void setOperands(Operand dest, int immediate) {
        if (dest == null) {
            throw new IllegalArgumentException("add immediate: dest cannot be null");
        }
        this.dest = dest;
        this.immediate = immediate;
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        if (dest == null) {
            throw new IllegalStateException("X86AddImmediateInstruction operands not initialized");
        }
        String indent = " ".repeat(numOfSpace);
        return String.format("%sadd %s, %d", indent, dest, immediate);
    }
}
