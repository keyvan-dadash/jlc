package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 not instruction: dest = ~dest
 * 
 * Syntax: not dest
 */
public class X86NotInstruction implements Instruction {
    private Operand dest;
    private int numOfSpace;

    public X86NotInstruction() {}

    public X86NotInstruction(Operand dest) {
        setOperand(dest);
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
            throw new IllegalStateException("X86NotInstruction operand not initialized");
        }
        String indent = " ".repeat(numOfSpace);
        return String.format("%snot %s", indent, dest);
    }
}