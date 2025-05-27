package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 SSE move instruction for floating-point:
 *   movss dest, src  (single-precision)
 *   movsd dest, src  (double-precision)
 *
 * dest must be an XMM register; src may be XMM register or memory.
 */
public class X86MoveFPInstruction implements Instruction {
    private final boolean isDouble;
    private Operand dest;
    private Operand src;
    private int numOfSpace;

    /** Construct for single-precision (movss). */
    public X86MoveFPInstruction() {
        this.isDouble = false;
    }

    /** Construct with precision and operands. */
    public X86MoveFPInstruction(boolean isDouble, Operand dest, Operand src) {
        this.isDouble = isDouble;
        setOperands(dest, src);
    }

    /**
     * Set or reset the operands.
     * @throws IllegalArgumentException if dest is not a register or
     *                                  if moving between two memory addresses.
     */
    public void setOperands(Operand dest, Operand src) {
        // disallow memory→memory
        if (dest.isMemory() && src.isMemory()) {
            throw new IllegalArgumentException("mov: cannot move memory to memory");
        }
        // ensure at least one side is a register
        if (!dest.isRegister() && !src.isRegister()) {
            throw new IllegalArgumentException(
                "mov" + (isDouble ? "sd" : "ss") + ": one operand must be a register");
        }
        this.dest = dest;
        this.src  = src;
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        if (dest == null || src == null) {
            throw new IllegalStateException("X86MoveFPInstruction operands not initialized");
        }
        String mnem   = isDouble ? "movsd" : "movss";
        String indent = " ".repeat(numOfSpace);
        return String.format("%s%s %s, %s",
                             indent,
                             mnem,
                             dest.toString(),
                             src.toString());
    }
}
