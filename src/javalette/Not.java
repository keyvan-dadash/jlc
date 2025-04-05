package javalette;

import java.util.HashMap;
import java.util.Map;

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

        throw new OperationError("operation not is not possible on " + var1.GetVariableType() + " and " + tmp_bool.GetVariableType());
    }
}

