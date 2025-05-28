package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 SSE add instruction for floating point:
 *   addss dest, src  (single)
 *   addsd dest, src  (double)
 *
 * dest must be a register; src may be register or memory.
 */
public class X86AddFPInstruction implements Instruction {
    private final boolean isDouble;
    private Operand dest;
    private Operand src;
    private int numOfSpace;

    public X86AddFPInstruction(boolean isDouble) {
        this.isDouble = isDouble;
    }

    public X86AddFPInstruction(boolean isDouble, Operand dest, Operand src) {
        this.isDouble = isDouble;
        setOperands(dest, src);
    }

    public void setOperands(Operand dest, Operand src) {
        if (!dest.isRegister()) {
            throw new IllegalArgumentException("add" + (isDouble ? "sd" : "ss") +
                ": destination must be a register");
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
            throw new IllegalStateException("X86AddFPInstruction operands not initialized");
        }
        String mnem = isDouble ? "addsd" : "addss";
        String indent = " ".repeat(numOfSpace);
        return String.format("%s%s %s, %s",
                indent, mnem, dest, src);
    }
}