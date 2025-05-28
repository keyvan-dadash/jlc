package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 SSE multiply instruction for floating point:
 *   mulss dest, src  (single)
 *   mulsd dest, src  (double)
 */
public class X86MulFPInstruction implements Instruction {
    private final boolean isDouble;
    private Operand dest;
    private Operand src;
    private int numOfSpace;

    public X86MulFPInstruction(boolean isDouble) {
        this.isDouble = isDouble;
    }

    public X86MulFPInstruction(boolean isDouble, Operand dest, Operand src) {
        this.isDouble = isDouble;
        setOperands(dest, src);
    }

    public void setOperands(Operand dest, Operand src) {
        if (!dest.isRegister()) {
            throw new IllegalArgumentException("mul" + (isDouble ? "sd" : "ss") +
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
            throw new IllegalStateException("X86MulFPInstruction operands not initialized");
        }
        String mnem = isDouble ? "mulsd" : "mulss";
        String indent = " ".repeat(numOfSpace);
        return String.format("%s%s %s, %s",
                indent, mnem, dest, src);
    }
}