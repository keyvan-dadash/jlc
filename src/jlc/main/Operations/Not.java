package jlc.main.Operations;

import java.util.HashMap;
import java.util.Map;

import jlc.main.Variables.Variable;
import jlc.main.Variables.BooleanVariable;
import jlc.main.Variables.VariableType;

public class Not implements Operation {

    public Map<VariableType, VariableType> execute_allowance;

    public Not() {
        execute_allowance = new HashMap<>();
        execute_allowance.put(VariableType.Boolean, VariableType.Boolean);
    }

    @Override
    public Variable Execute(Variable var1, Variable var2) {
        VariableType allowed_type = execute_allowance.get(var1.GetVariableType());
        BooleanVariable tmp_bool = new BooleanVariable("tmp");
        if (tmp_bool.GetVariableType() == allowed_type) {
            return tmp_bool.GetNewVariableSameType();
        }

        String error_str = String.format(
            "operation not is not possible on %s(%s)",
            var1.GetVariableName(),
            var1.GetVariableType());
        throw new OperationError(error_str);
    }
}

