package jlc.main.Operations;

import java.util.HashMap;
import java.util.Map;

import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableType;

public class Neg implements Operation {

    public Map<VariableType, VariableType> execute_allowance;

    public Neg() {
        execute_allowance = new HashMap<>();

        execute_allowance.put(VariableType.Int, VariableType.Int);
        execute_allowance.put(VariableType.Double, VariableType.Double);
    }

    @Override
    public Variable Execute(Variable var1, Variable var2) {
        VariableType allowed_type = execute_allowance.get(var1.GetVariableType());
        if (allowed_type != null) {
            return var1.GetNewVariableSameType();
        }

        String error_str = String.format(
            "operation neg is not possible on %s(%s)",
            var1.GetVariableName(),
            var1.GetVariableType());
        throw new OperationError(error_str);
    }
}