package jlc.main.Instructions.x86;

/**
 * Represents an x86 memory operand, either as:
 *   - base+offset ([rbp-4], etc.)
 *   - rip-relative ([rel foo])
 *   - raw ([foo+8])
 *
 * And with an optional PTR-size prefix (BYTE/DWORD/QWORD PTR).
 */
public class Address {
    private final Register base;      // null if raw-expression
    private final int offset;         // only if base != null
    private final String expression;  // non-null if raw or rip-label
    private final MemSize size;       // size prefix

    // base+offset form:
    public Address(Register base, int offset, MemSize size) {
        this.base       = base;
        this.offset     = offset;
        this.expression = null;
        this.size       = size;
    }

    // rip-relative label form:
    public Address(String label, MemSize size) {
        this.base       = null;
        this.offset     = 0;
        this.expression = label;
        this.size       = size;
    }

    // rip-relative label form:
    public Address(Register base, String label, MemSize size) {
        this.base       = base;
        this.offset     = 0;
        this.expression = label;
        this.size       = size;
    }

    public Address(Register base, int offset) {
        this(base, offset, MemSize.QWORD);
    }

    public Address(String rawExpr) {
        this(rawExpr, null);
    }

    @Override
    public String toString() {
        String inner;
        if (expression != null) {
            // "[rel foo]" or raw "[foo+8]"
            if (base == Register.RIP) {
                inner = String.format("[%s %s]", "rel", expression);
            } else {
                inner = expression;
            }
        } else {
            if      (offset > 0) inner = String.format("[%s+%d]", base.getName(), offset);
            else if (offset < 0) inner = String.format("[%s-%d]", base.getName(), -offset);
            else                 inner = String.format("[%s]",     base.getName());
        }
        return (size != null ? size + " " : "") + inner;
    }

    public MemSize getSize() {
        return size;
    }
}
