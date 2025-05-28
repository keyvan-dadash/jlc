package jlc.main.Instructions.x86;

import jlc.main.Variables.DoubleVariable;
import jlc.main.Variables.IntVariable;
import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableKind;
import jlc.main.Variables.VariableType;

/**
 * Utility methods for convenient code generation.
 */
public class Utils {
    private Utils() {}

    public static boolean isTemporaryVariable(Variable var) {
        if (var == null) return false;
        String name = var.GetVariableName();
        return name != null && name.matches("t\\d+");
    }

    public static boolean isGlobalVariable(Variable var) {
        if (var == null) return false;
        String name = var.GetVariableName();
        return name != null && name.matches("g\\d+");
    }

    public static boolean isVirtualVariable(Variable var) {
        return isTemporaryVariable(var) || isGlobalVariable(var);
    }

    // not used
    public static int bitsForType(jlc.main.Variables.VariableType type) {
        switch (type) {
            case Int:
                return 32;
            case Boolean:
                return 8;
            case Double:
                return 32;
            case String:
                return 64;
            default:
                throw new IllegalArgumentException("Unsupported type for bit-size: " + type);
        }
    }

    // not used
    public static int bitsForVariable(Variable var) {
        if (var == null) {
            throw new IllegalArgumentException("Variable cannot be null");
        }
        return bitsForType(var.GetVariableType());
    }

    public static MemSize memSizeForType(VariableType type) {
        switch (type) {
            case Boolean: return MemSize.BYTE;
            case Int:     return MemSize.DWORD;
            case Double:  return MemSize.DWORD;
            case String:  return MemSize.QWORD;
            default:
                throw new IllegalArgumentException("No PTR‐size for type: " + type);
        }
    }

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
