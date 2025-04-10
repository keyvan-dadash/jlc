package jlc.main.Instructions.LLVM;

import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableKind;
import jlc.main.Variables.VariableType;

public class Utils {
    public static String VariableToLLVMVariable(Variable var) {
        if (var.GetVariableKind() == VariableKind.ConstantVariable) {
            return var.GetVariableName();
        }
        
        return String.format("%%%s", var.GetVariableName());
    }

    public static String VariableTypeToLLVMVariableType(VariableType varType) {
        switch (varType) {
            case Int: {
                return "i32";
            }
            case Boolean: {
                return "i1";
            }
            case Double: {
                return "double";
            }
            default: {
                throw new RuntimeException("other variable types cannot be converted to llvm variable type");
            }
        }
    }

    public static String GetNumOfSpace(int num) {
        if (num == 0) {
            return "";
        }

        String format = "%" + num + "s";
        return String.format(format, "");
    }
}
