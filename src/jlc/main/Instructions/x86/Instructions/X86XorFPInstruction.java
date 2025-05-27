package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 SSE XOR instruction for floating‐point bitwise XOR:
 *   xorps dest, src   (single-precision)
 *   xorpd dest, src   (double-precision)
 *
 */
public class X86XorFPInstruction implements Instruction {
    private final boolean isDouble;
    private Operand dest;
    private Operand src;
    private int numOfSpace;

    public X86XorFPInstruction(boolean isDouble) {
        this.isDouble = isDouble;
    }

    public X86XorFPInstruction(boolean isDouble, Operand dest, Operand src) {
        this.isDouble = isDouble;
        setOperands(dest, src);
    }

    public void setOperands(Operand dest, Operand src) {
        if (!dest.isRegister()) {
            String mnem = isDouble ? "xorpd" : "xorps";
            throw new IllegalArgumentException(mnem + ": destination must be an XMM register");
        }
        if (dest.isMemory() && src.isMemory()) {
            throw new IllegalArgumentException("xorfp: cannot xor memory to memory");
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
            throw new IllegalStateException("X86XorFPInstruction operands not initialized");
        }
        String mnem = isDouble ? "xorpd" : "xorps";
        String indent = " ".repeat(numOfSpace);
        return String.format("%s%s %s, %s", indent, mnem, dest, src);
    }
}
