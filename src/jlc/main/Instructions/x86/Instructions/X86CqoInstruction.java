package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;

/**
 * x86 CQO instruction (sign‐extend RAX → RDX:RAX).
 *
 * In 64-bit mode this is `cqo`.  In 32-bit mode you'd use `cdq`.
 */
public class X86CqoInstruction implements Instruction {
    private int numOfSpace;

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        String indent = " ".repeat(numOfSpace);
        return indent + "cqo";
    }
}
