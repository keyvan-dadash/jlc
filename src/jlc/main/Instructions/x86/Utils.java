package jlc.main.Instructions.x86;

import jlc.main.Variables.DoubleVariable;
import jlc.main.Variables.IntVariable;
import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableKind;
import jlc.main.Variables.VariableType;

/**
 * Utility methods for identifying kinds of IR variables by naming convention.
 */
public class Utils {
    private Utils() {
        // no instances
    }

    /**
     * Returns true if the variable is a temporary: name matches t<digits> (e.g., t0, t102).
     */
    public static boolean isTemporaryVariable(Variable var) {
        if (var == null) return false;
        String name = var.GetVariableName();
        return name != null && name.matches("t\\d+");
    }

    /**
     * Returns true if the variable is global: name matches g<digits> (e.g., g0, g16).
     */
    public static boolean isGlobalVariable(Variable var) {
        if (var == null) return false;
        String name = var.GetVariableName();
        return name != null && name.matches("g\\d+");
    }

    /**
     * Returns true if the variable is virtual (either temporary or global).
     */
    public static boolean isVirtualVariable(Variable var) {
        return isTemporaryVariable(var) || isGlobalVariable(var);
    }

    /**
     * Returns the number of bits required to store a value of the given type.
     * Supports Int, Boolean, and Double. All sizes are at most 32 bits.
     * @param type the variable type
     * @return bit-width for memory storage
     */
    public static int bitsForType(jlc.main.Variables.VariableType type) {
        switch (type) {
            case Int:
                return 32;
            case Boolean:
                return 8;
            case Double:
                return 32;
            case String:
                return 64;    // strings passed/loaded via a 64-bit pointer
            default:
                throw new IllegalArgumentException("Unsupported type for bit-size: " + type);
        }
    }

    /**
     * Returns the number of bits required to store the given variable in memory.
     * Delegates to bitsForType based on the variable's type.
     * @param var the variable whose type to inspect
     * @return bit-width for memory storage
     */
    public static int bitsForVariable(Variable var) {
        if (var == null) {
            throw new IllegalArgumentException("Variable cannot be null");
        }
        return bitsForType(var.GetVariableType());
    }

    /**
     * Returns the PTR‐size enum for the given VariableType.
     */
    public static MemSize memSizeForType(VariableType type) {
        switch (type) {
            case Boolean: return MemSize.BYTE;
            case Int:     return MemSize.DWORD;
            case Double:  return MemSize.DWORD;  // our “double” is 32-bit
            case String:  return MemSize.QWORD;  // pointers on x86-64
            default:
                throw new IllegalArgumentException("No PTR‐size for type: " + type);
        }
    }

    /**
     * Returns the PTR‐size enum for the given Variable.
     */
    public static MemSize memSizeForVariable(Variable var) {
        if (var == null) throw new IllegalArgumentException("Variable cannot be null");
        return memSizeForType(var.GetVariableType());
    }

    public static boolean isAddressVariable(Variable var) {
        if (var == null) return false;
        String name = var.GetVariableName();
        return name != null && name.contains("[") && name.contains("]");
    }

    public static int alignTo16(int size) {
        if (size <= 0) return 0;
        return ((size + 15) / 16) * 16;
    }

    public static Variable GetDefaultValueOfVariableType(VariableType varType) {
        switch (varType) {
            case Int: {
                Variable var = new IntVariable("0");
                var.SetVariableKind(VariableKind.ConstantVariable);
                return var;
            }
            case Boolean: {
                Variable var = new IntVariable("0");
                var.SetVariableKind(VariableKind.ConstantVariable);
                return var;
            }
            case Double: {
                Variable var = new DoubleVariable("0.0");
                var.SetVariableKind(VariableKind.ConstantVariable);
                return var;
            }
            default: {
                throw new RuntimeException("initial value for this variable type does not especified in the language");
            }
        }
    }
}
