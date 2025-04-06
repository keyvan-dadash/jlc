package jlc.main.Operations;

import java.util.HashMap;
import java.util.Map;

import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableType;

public class Add implements Operation {

    public Map<VariableType, VariableType> execute_allowance;

    public AddType typeOfOperation;

    public Add(AddType typeOfOperation) {
        execute_allowance = new HashMap<>();

        this.typeOfOperation = typeOfOperation;
        execute_allowance.put(VariableType.Int, VariableType.Int);
        execute_allowance.put(VariableType.Double, VariableType.Double);
    }

    @Override
    public Variable Execute(Variable var1, Variable var2) {
        VariableType allowed_type = execute_allowance.get(var1.GetVariableType());
        if (var2.GetVariableType() == allowed_type) {
            return var2.GetNewVariableSameType();
        }

        String error_str = String.format(
            "operation add is not possible on %s(%s) and %s(%s)",
            var1.GetVariableName(),
            var1.GetVariableType(),
            var2.GetVariableName(), 
            var2.GetVariableType());
        throw new OperationError(error_str);
    }
}