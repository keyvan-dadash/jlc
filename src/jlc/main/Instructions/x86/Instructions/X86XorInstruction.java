package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 XOR instruction for integers:
 *   xor dest, src
 */
public class X86XorInstruction implements Instruction {
    private Operand dest;
    private Operand src;
    private int numOfSpace;

    public X86XorInstruction() {}

    public X86XorInstruction(Operand dest, Operand src) {
        setOperands(dest, src);
    }

    public void setOperands(Operand dest, Operand src) {
        if (dest.isMemory() && src.isMemory()) {
            throw new IllegalArgumentException("xor: cannot xor memory to memory");
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
            throw new IllegalStateException("X86XorInstruction operands not initialized");
        }
        String indent = " ".repeat(numOfSpace);
        return String.format("%sxor %s, %s", indent, dest, src);
    }
}
