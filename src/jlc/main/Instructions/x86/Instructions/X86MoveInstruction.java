package jlc.main.Instructions.x86.Instructions;

import javax.swing.text.Utilities;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;
import jlc.main.Instructions.x86.Register;
import jlc.main.Instructions.x86.Utils;
import jlc.main.Instructions.x86.MemSize;

/**
 * x86 mov-like instruction: copies src → dest.
 *   - If dest is a register:
 *       • If src is 8 or 16 bits, uses movzx into full-64-bit dest  
 *       • If src is 32 bits, uses movsxd into full-64-bit dest  
 *       • If src is already 64 bits, uses plain mov  
 *   - Otherwise (memory destination), always plain mov  
 * Illegal: memory→memory.
 */
public class X86MoveInstruction implements Instruction {
    private final Operand dest;
    private final Operand src;
    private int numOfSpace;

    public X86MoveInstruction(Operand dest, Operand src) {
        if (dest.isMemory() && src.isMemory()) {
            throw new IllegalArgumentException("mov: cannot move memory to memory");
        }
        this.dest = dest;
        this.src  = src;
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        String indent = " ".repeat(numOfSpace);

        // figure out the bit-width of the source
        int srcWidth = src.isRegister()
            ? src.getRegister().getWidth()
            : src.getSize().bitWidth();

        // figure out the bit-width of the destination
        int destWidth = dest.isRegister()
            ? dest.getRegister().getWidth()
            : dest.getSize().bitWidth();

        String mnem;
        if (dest.isRegister() && (src.isMemory() || src.isRegister())) {
            if (srcWidth < 64) {
                if (srcWidth == 32) {
                    mnem = "movsxd";   // sign-extend 32→64
                } else {
                    mnem = "movzx";    // zero-extend 8 or 16 → 64
                }
            } else {
                mnem = "mov";         // same width
            }
        } else {
            // memory destination or immediate source → plain mov
            mnem = "mov";
        }

        return String.format("%s%s %s, %s",
                             indent,
                             mnem,
                             formatDest(),
                             formatSrc());
    }

    private String formatDest() {
        if (dest.isRegister()) {
            // always emit the full-width (64-bit) name for the dest
            Register r64 = dest.getRegister().forMemSize(MemSize.QWORD);
            return r64.getName();
        } else {
            // memory will already include its PTR-size prefix in Address.toString()
            return dest.toString();
        }
    }

    private String formatSrc() {
        if (src.isRegister()) {
            // figure out the bit-width of the source
            int srcWidth = src.isRegister()
                ? src.getRegister().getWidth()
                : src.getSize().bitWidth();

            // figure out the bit-width of the destination
            int destWidth = dest.isRegister()
                ? dest.getRegister().getWidth()
                : dest.getSize().bitWidth();

            if (!dest.isRegister() && destWidth < srcWidth) {
                return src.getRegister().forMemSize(dest.getSize()).getName();
            }

            // source register always by its natural name
            return src.getRegister().getName();
        } else {
            // memory or immediate prints itself
            return src.toString();
        }
    }
}
