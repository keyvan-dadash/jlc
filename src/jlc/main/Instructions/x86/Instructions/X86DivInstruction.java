package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 idiv instruction (signed). It uses RAX and RDX
 *
 * Syntax: idiv src
 * 
 */
public class X86DivInstruction implements Instruction {
    private Operand src;
    private int numOfSpace;

    public X86DivInstruction() {}

    public X86DivInstruction(Operand src) {
        setOperands(src);
    }

    public void setOperands(Operand src) {
        this.src = src;
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        if (src == null) {
            throw new IllegalStateException("X86DivInstruction operand not initialized");
        }
        String indent = " ".repeat(numOfSpace);
        return String.format("%sidiv %s", indent, src);
    }
}
