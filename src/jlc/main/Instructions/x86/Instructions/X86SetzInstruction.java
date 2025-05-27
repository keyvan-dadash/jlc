package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 setz instruction:
 *   setz dest
 *
 * Sets the low‐order byte of dest to 1 if ZF=1, or 0 otherwise.
 * dest must be an 8-bit register (e.g. AL, BL, CL, DL).
 */
public class X86SetzInstruction implements Instruction {
    private Operand dest;
    private int numOfSpace;

    /** Empty ctor; must call setOperand() before GenerateInstruction(). */
    public X86SetzInstruction() {}

    /**
     * @param dest  an 8-bit register operand
     */
    public X86SetzInstruction(Operand dest) {
        setOperand(dest);
    }

    /**
     * Set or reset the destination operand.
     * @throws IllegalArgumentException if dest is not an 8-bit register
     */
    public void setOperand(Operand dest) {
        if (!dest.isRegister()) {
            throw new IllegalArgumentException("setz: destination must be a register");
        }
        // enforce 8-bit
        if (dest.getRegister().getWidth() != 8) {
            throw new IllegalArgumentException(
                "setz: destination must be an 8-bit register (AL, BL, CL, DL)");
        }
        this.dest = dest;
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        if (dest == null) {
            throw new IllegalStateException("X86SetzInstruction destination not initialized");
        }
        String indent = " ".repeat(numOfSpace);
        return String.format("%ssetz %s", indent, dest);
    }
}
