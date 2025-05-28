package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 pop instruction:
 *   pop dest
 */
public class X86PopInstruction implements Instruction {
    private Operand dest;
    private int numOfSpace;

    public X86PopInstruction() {}

    public X86PopInstruction(Operand dest) {
        this.dest = dest;
    }

    public void setOperand(Operand dest) {
        this.dest = dest;
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        if (dest == null) {
            throw new IllegalStateException("X86PopInstruction: operand not specified");
        }
        String indent = " ".repeat(numOfSpace);
        return String.format("%spop %s", indent, dest);
    }
}