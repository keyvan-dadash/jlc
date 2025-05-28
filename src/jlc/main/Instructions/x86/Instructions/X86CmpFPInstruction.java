package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 SSE compare instruction for floating-point:
 *   ucomiss src1, src2   (single-precision)
 *   ucomisd src1, src2   (double-precision)
 *
 */
public class X86CmpFPInstruction implements Instruction {
    private final boolean isDouble;
    private Operand src1;
    private Operand src2;
    private int numOfSpace;

    public X86CmpFPInstruction() {
        this.isDouble = false;
    }

    public X86CmpFPInstruction(boolean isDouble, Operand src1, Operand src2) {
        this.isDouble = isDouble;
        setOperands(src1, src2);
    }

    public void setOperands(Operand src1, Operand src2) {
        if (!src1.isRegister()) {
            throw new IllegalArgumentException(
                (isDouble ? "ucomisd" : "ucomiss") + ": first operand must be XMM register"
            );
        }
        if (src1.isMemory() && src2.isMemory()) {
            throw new IllegalArgumentException("cmpfp: cannot compare memory to memory");
        }
        this.src1 = src1;
        this.src2 = src2;
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        if (src1 == null || src2 == null) {
            throw new IllegalStateException("X86CmpFPInstruction operands not initialized");
        }
        String mnem   = isDouble ? "ucomisd" : "ucomiss";
        String indent = " ".repeat(numOfSpace);
        return String.format("%s%s %s, %s",
                             indent,
                             mnem,
                             src1.toString(),
                             src2.toString());
    }
}
