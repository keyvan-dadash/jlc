package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 test instruction:
 *   test src1, src2
 * This will be used for branchings
 */
public class X86TestInstruction implements Instruction {
    private Operand src1;
    private Operand src2;
    private int numOfSpace;

    public X86TestInstruction() {}

    public X86TestInstruction(Operand src1, Operand src2) {
        setOperands(src1, src2);
    }

    public void setOperands(Operand src1, Operand src2) {
        if (src1.isMemory() && src2.isMemory()) {
            throw new IllegalArgumentException("test: cannot test memory against memory");
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
            throw new IllegalStateException("X86TestInstruction operands not initialized");
        }
        String indent = " ".repeat(numOfSpace);
        return String.format("%stest %s, %s", indent, src1, src2);
    }
}