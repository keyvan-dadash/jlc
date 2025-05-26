package jlc.main.Operations;

import java.util.HashMap;
import java.util.Map;

import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableType;

public class ArrAss implements Operation {

    public Map<VariableType, VariableType> execute_allowance;

    public ArrAss() {
        execute_allowance = new HashMap<>();

        execute_allowance.put(VariableType.Int, VariableType.Int);
        execute_allowance.put(VariableType.Double, VariableType.Double);
        execute_allowance.put(VariableType.Boolean, VariableType.Boolean);
    }

    @Override
    public Variable Execute(Variable var1, Variable var2) {
        VariableType allowed_type = execute_allowance.get(var1.GetVariableType());
        if (var2.GetVariableType() == allowed_type) {
            return var2.GetNewVariableSameType();
        }

        String error_str = String.format(
            "operation ass is not possible on %s(%s) and %s(%s)",
            var1.GetVariableName(),
            var1.GetVariableType(),
            var2.GetVariableName(), 
            var2.GetVariableType());
        throw new OperationError(error_str);
    }
}