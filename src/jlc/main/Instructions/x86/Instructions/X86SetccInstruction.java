package jlc.main.Instructions.x86.Instructions;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;
import jlc.main.Operations.RelType;

/**
 * x86 SETcc instruction: set byte if condition holds.
 * dest must be a byte-addressable register (e.g. AL, BL, CL, DL).
 *
 * RelType → mnemonic:
 *   LTH  → setl
 *   LE   → setle
 *   GTH  → setg
 *   GE   → setge
 *   EQU  → sete
 *   NE   → setne
 */
public class X86SetccInstruction implements Instruction {
    private final RelType cond;
    private Operand dest;
    private int numOfSpace;

    /** Construct with just the condition; must call setOperand() before GenerateInstruction(). */
    public X86SetccInstruction(RelType cond) {
        this.cond = cond;
    }

    /** Construct with condition and destination operand. */
    public X86SetccInstruction(RelType cond, Operand dest) {
        this(cond);
        setOperand(dest);
    }

    /** Destination must be a register (byte‐addressable). */
    public void setOperand(Operand dest) {
        if (!dest.isRegister()) {
            throw new IllegalArgumentException("set" + cond.name().toLowerCase() +
                ": destination must be a register");
        }
        this.dest = dest;
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        if (dest == null) {
            throw new IllegalStateException("X86SetccInstruction operand not initialized");
        }
        String mnem;
        switch (cond) {
            case LTH:  mnem = "setl";  break;
            case LE:   mnem = "setle"; break;
            case GTH:  mnem = "setg";  break;
            case GE:   mnem = "setge"; break;
            case EQU:  mnem = "sete";  break;
            case NE:   mnem = "setne"; break;
            default:   throw new IllegalArgumentException("Unsupported condition: " + cond);
        }
        String indent = " ".repeat(numOfSpace);
        return String.format("%s%s %s", indent, mnem, dest);
    }
}
