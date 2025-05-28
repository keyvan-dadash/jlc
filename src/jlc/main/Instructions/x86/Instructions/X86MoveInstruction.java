package jlc.main.Instructions.x86.Instructions;

import javax.swing.text.Utilities;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Operand;
import jlc.main.Instructions.x86.Register;
import jlc.main.Instructions.x86.Utils;
import jlc.main.Instructions.x86.MemSize;

/**
 * x86 mov-like instruction: move src to dest
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

        int srcWidth = src.isRegister()
            ? src.getRegister().getWidth()
            : src.getSize().bitWidth();

        int destWidth = dest.isRegister()
            ? dest.getRegister().getWidth()
            : dest.getSize().bitWidth();

        String mnem;
        if (dest.isRegister() && (src.isMemory() || src.isRegister())) {
            if (srcWidth < 64) {
                if (srcWidth == 32) {
                    mnem = "movsxd";
                } else {
                    mnem = "movzx";
                }
            } else {
                mnem = "mov";
            }
        } else {
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
            Register r64 = dest.getRegister().forMemSize(MemSize.QWORD);
            return r64.getName();
        } else {
            return dest.toString();
        }
    }

    private String formatSrc() {
        if (src.isRegister()) {
            int srcWidth = src.isRegister()
                ? src.getRegister().getWidth()
                : src.getSize().bitWidth();

            int destWidth = dest.isRegister()
                ? dest.getRegister().getWidth()
                : dest.getSize().bitWidth();

            if (!dest.isRegister() && destWidth < srcWidth) {
                return src.getRegister().forMemSize(dest.getSize()).getName();
            }

            return src.getRegister().getName();
        } else {
            return src.toString();
        }
    }
}
