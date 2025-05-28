package jlc.main.Instructions.x86;

import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableType;

/**
 * Wraps a register, memory operand, or an immediate.
 */
public class Operand {
    private final Register reg;
    private final Address addr;
    private final Variable immVar;
    private final MemSize size;

    private Operand(Register reg, Address addr, Variable immVar, MemSize size) {
        this.reg    = reg;
        this.addr   = addr;
        this.immVar = immVar;
        this.size = size;
    }

    /** Register operand. */
    public static Operand of(Register r) {
        return new Operand(r, null, null, MemSize.fromBits(r.getWidth()));
    }

    /** Memory operand from an Address. */
    public static Operand of(Address a) {
        return new Operand(null, a, null, a.getSize());
    }

    /**
     * Memory operand for a local/global Variable whose
     * name is already “[ebp-4]” or similar from FuncDefIR.
     */
    public static Operand ofMemory(Variable v) {
        MemSize size;
        switch (v.GetVariableType()) {
            case Boolean: size = MemSize.BYTE;  break;
            case Int:     size = MemSize.DWORD; break;
            case Double:  size = MemSize.QWORD; break;
            default:      size = MemSize.QWORD; break;
        }
        return new Operand(null, new Address(v.GetVariableName(), size), v, size);
    }

    /**
     * Memory operand for a global label “foo”. 
     * Later it uses rel instruction.
     */
    public static Operand ofGlobal(String label, VariableType vt) {
        MemSize size;
        switch (vt) {
            case Boolean: size = MemSize.BYTE;  break;
            case Int:     size = MemSize.DWORD; break;
            case Double:  size = MemSize.QWORD; break;
            default:      size = MemSize.QWORD; break;
        }
        return new Operand(null, new Address(Register.RIP, label, size), null, size);
    }

    /**
     * Operand of immediate (constant) variables.
     */
    public static Operand ofImmediate(Variable constVar) {
        return new Operand(null, null, constVar, Utils.memSizeForVariable(constVar));
    }

    public boolean isRegister() { return reg != null; }
    public boolean isMemory()   { return addr != null; }

    @Override
    public String toString() {
        if (isRegister())   return reg.getName();
        if (isMemory())     return addr.toString();
        return immVar.GetVariableName();
    }

    public MemSize getSize() {
        return size;
    }

    public Register getRegister() {
        return reg;
    }
}
