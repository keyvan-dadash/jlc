package jlc.main.Operations;

import java.util.HashMap;
import java.util.Map;

import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableType;

public class Ass implements Operation {

    public Map<VariableType, VariableType> execute_allowance;

    public Ass() {
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

        throw new OperationError("operation ass is not possible on " + var1.GetVariableType() + " and " + var2.GetVariableType());
    }
}