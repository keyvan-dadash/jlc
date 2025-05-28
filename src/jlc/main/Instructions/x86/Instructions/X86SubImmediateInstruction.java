package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 subtract‐immediate instruction:
 *    sub dest, imm
 *
 */
public class X86SubImmediateInstruction implements Instruction {
    private Operand dest;
    private int immediate;
    private int numOfSpace;

    public X86SubImmediateInstruction() {}

    public X86SubImmediateInstruction(Operand dest, int imm) {
        setOperands(dest, imm);
    }

    public void setOperands(Operand dest, int imm) {
        this.dest = dest;
        this.immediate = imm;
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        if (dest == null) {
            throw new IllegalStateException("X86SubImmediateInstruction operands not initialized");
        }
        String indent = " ".repeat(numOfSpace);
        return String.format("%ssub %s, %d", indent, dest, immediate);
    }
}
