package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 setz instruction:
 *   setz dest
 *
 * Sets the byte of dest to 1 if flag of ZF is 1. Otherwise, 0.
 * dest should be 8bit register.
 */
public class X86SetzInstruction implements Instruction {
    private Operand dest;
    private int numOfSpace;

    public X86SetzInstruction() {}

    public X86SetzInstruction(Operand dest) {
        setOperand(dest);
    }

    public void setOperand(Operand dest) {
        if (!dest.isRegister()) {
            throw new IllegalArgumentException("setz: destination must be a register");
        }

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
