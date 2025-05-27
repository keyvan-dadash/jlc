package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 NEG instruction: two’s-complement negate.
 * Syntax:    neg dest
 * Note: currently this class does not have any use.
 */
public class X86NegInstruction implements Instruction {
    private Operand operand;
    private int numOfSpace;

    public X86NegInstruction() {}

    public X86NegInstruction(Operand operand) {
        setOperand(operand);
    }

    public void setOperand(Operand operand) {
        this.operand = operand;
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        if (operand == null) {
            throw new IllegalStateException("X86NegInstruction operand not initialized");
        }
        String indent = " ".repeat(numOfSpace);
        return String.format("%sneg %s", indent, operand);
    }
}
