package jlc.main.Instructions.x86;

public enum MemSize {
    BYTE("BYTE"),
    DWORD("DWORD"),
    QWORD("QWORD");

    private final String asm;
    MemSize(String asm) { this.asm = asm; }
    @Override public String toString() { return asm; }

    /** Number of bits this size represents. */
    public int bitWidth() {
        switch (this) {
            case BYTE:  return 8;
            case DWORD: return 32;
            case QWORD: return 64;
            default:    throw new IllegalStateException("Unexpected MemSize: " + this);
        }
    }

    /** 
     * Map a bit-width back to the corresponding MemSize.
     * @throws IllegalArgumentException if bits is not 8, 32, or 64 
     */
    public static MemSize fromBits(int bits) {
        switch (bits) {
            case 8:  return BYTE;
            case 32: return DWORD;
            case 64: return QWORD;
            case 128: // XMM registers are 128-bit wide, but memory ops stay QWORD
                return QWORD;
            default: throw new IllegalArgumentException("No MemSize for bit width: " + bits);
        }
    }
}