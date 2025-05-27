package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 SSE subtract instruction for floating point:
 *   subss dest, src  (single)
 *   subsd dest, src  (double)
 */
public class X86SubFPInstruction implements Instruction {
    private final boolean isDouble;
    private Operand dest;
    private Operand src;
    private int numOfSpace;

    public X86SubFPInstruction(boolean isDouble) {
        this.isDouble = isDouble;
    }

    public X86SubFPInstruction(boolean isDouble, Operand dest, Operand src) {
        this.isDouble = isDouble;
        setOperands(dest, src);
    }

    public void setOperands(Operand dest, Operand src) {
        if (!dest.isRegister()) {
            throw new IllegalArgumentException("sub" + (isDouble ? "sd" : "ss") +
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
            throw new IllegalStateException("X86SubFPInstruction operands not initialized");
        }
        String mnem = isDouble ? "subsd" : "subss";
        String indent = " ".repeat(numOfSpace);
        return String.format("%s%s %s, %s",
                indent, mnem, dest, src);
    }
}