package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 add immediate instruction:
 *    add dest, imm
 *
 */
public class X86AddImmediateInstruction implements Instruction {
    private Operand dest;
    private int immediate;
    private int numOfSpace;

    public X86AddImmediateInstruction() {}

    public X86AddImmediateInstruction(Operand dest, int immediate) {
        setOperands(dest, immediate);
    }

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
