package jlc.main.Instructions.LLVM;

import jlc.main.Variables.ArrayVariable;
import jlc.main.Variables.BooleanVariable;
import jlc.main.Variables.DoubleVariable;
import jlc.main.Variables.IntVariable;
import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableKind;
import jlc.main.Variables.VariableType;

public class Utils {
    public static String VariableToLLVMVariable(Variable var) {
        if (var.GetVariableKind() == VariableKind.ConstantVariable) {
            return var.GetVariableName();
        }

        if (var.GetVariableKind() == VariableKind.GlobalVariable) {
            return String.format("@%s", var.GetVariableName());
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
            case String: {
                return "i8*";
            }
            case Array: {
                return "i8*";
            }
            default: {
                throw new RuntimeException(String.format("other variable types cannot be converted to llvm variable type. %s", varType));
            }
        }
    }

    public static Variable GetDefaultValueOfVariableType(Variable variable) {
        switch (variable.GetVariableType()) {
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
            case Array: {
                Variable var = new ArrayVariable("null", variable.GetArrayType().GetVariableType());
                var.SetVariableKind(VariableKind.ConstantVariable);
                return var;
            }
            default: {
                throw new RuntimeException("initial value for this variable type does not especified in the language");
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

    public static Variable GetOne() {
        Variable one = new BooleanVariable("1");
        one.SetVariableKind(VariableKind.ConstantVariable);
        return one;
    }
}
