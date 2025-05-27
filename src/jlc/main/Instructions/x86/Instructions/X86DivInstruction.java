package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;

/**
 * x86 idiv instruction (signed): divide RDX:RAX by src → quotient in RAX, remainder in RDX.
 *
 * Syntax: idiv src
 * Illegal: src must not be memory-to-memory (but memory src is fine if dest is implicit RAX/RDX).
 */
public class X86DivInstruction implements Instruction {
    private Operand src;
    private int numOfSpace;

    public X86DivInstruction() {}

    /**
     * Ctor with the explicit divisor.
     * @param src register or memory operand
     */
    public X86DivInstruction(Operand src) {
        setOperands(src);
    }

    /**
     * Set or reset the divisor operand.
     *
     * @param src  the divisor (must be register or memory, but not memory→memory)
     */
    public void setOperands(Operand src) {
        // Note: on x86 both register and memory sources are allowed here.
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
        // Use "idiv" mnemonic for signed divide
        return String.format("%sidiv %s", indent, src);
    }
}
