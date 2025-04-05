package javalette;

import java.util.HashMap;
import java.util.Map;

public class Div implements Operation {

    public Map<VariableType, VariableType> execute_allowance;
    
    public Div() {
        execute_allowance = new HashMap<>();

        execute_allowance.put(VariableType.Int, VariableType.Int);
        execute_allowance.put(VariableType.Double, VariableType.Double);
    }

    @Override
    public Variable Execute(Variable var1, Variable var2) {
        VariableType allowed_type = execute_allowance.get(var1.GetVariableType());
        if (var2.GetVariableType() == allowed_type) {
            return var2.GetNewVariableSameType();
        }

        throw new OperationError("operation div is not possible on " + var1.GetVariableType() + " and " + var2.GetVariableType());
    }
}
