package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 cmp instruction:
 *   cmp src1, src2
 * 
 */
public class X86CmpInstruction implements Instruction {
    private Operand src1;
    private Operand src2;
    private int numOfSpace;

    public X86CmpInstruction() {}

    public X86CmpInstruction(Operand src1, Operand src2) {
        setOperands(src1, src2);
    }

    public void setOperands(Operand src1, Operand src2) {
        if (src1.isMemory() && src2.isMemory()) {
            throw new IllegalArgumentException("cmp: cannot compare memory to memory");
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
            throw new IllegalStateException("X86CmpInstruction operands not initialized");
        }
        String indent = " ".repeat(numOfSpace);
        return String.format("%scmp %s, %s", indent, src1, src2);
    }
}