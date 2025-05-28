package jlc.main.Instructions.x86;

/**
 * Helper for getting right keyword for a certain size in the code generation phase.
 *
 */
public enum MemSize {
    BYTE("BYTE"),
    DWORD("DWORD"),
    QWORD("QWORD");

    private final String asm;
    MemSize(String asm) { this.asm = asm; }
    @Override public String toString() { return asm; }

    public int bitWidth() {
        switch (this) {
            case BYTE:  return 8;
            case DWORD: return 32;
            case QWORD: return 64;
            default:    throw new IllegalStateException("Unexpected MemSize: " + this);
        }
    }

    public static MemSize fromBits(int bits) {
        switch (bits) {
            case 8:  return BYTE;
            case 32: return DWORD;
            case 64: return QWORD;
            case 128: // 128 bit should be QWORD since we cannot access memory of 128 bit
                return QWORD;
            default: throw new IllegalArgumentException("No MemSize for bit width: " + bits);
        }
    }
}