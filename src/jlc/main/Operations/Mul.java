package jlc.main.Operations;

import java.util.HashMap;
import java.util.Map;

import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableType;

public class Mul implements Operation {

    public Map<MulType, Map<VariableType, VariableType>> execute_allowance;

    public MulType typeOfOperation;

    public Mul(MulType typeOfOperation) {
        execute_allowance = new HashMap<>();

        this.typeOfOperation = typeOfOperation;

        // Times operation
        execute_allowance.put(MulType.Times, new HashMap<>());
        Map<VariableType, VariableType> execute_allowance_for_times = execute_allowance.get(MulType.Times);

        execute_allowance_for_times.put(VariableType.Int, VariableType.Int);
        execute_allowance_for_times.put(VariableType.Double, VariableType.Double);

        // Div operation
        execute_allowance.put(MulType.Div, new HashMap<>());
        execute_allowance_for_times = execute_allowance.get(MulType.Div);

        execute_allowance_for_times.put(VariableType.Int, VariableType.Int);
        execute_allowance_for_times.put(VariableType.Double, VariableType.Double);

        // Mod operation
        execute_allowance.put(MulType.Mod, new HashMap<>());
        execute_allowance_for_times = execute_allowance.get(MulType.Mod);

        execute_allowance_for_times.put(VariableType.Int, VariableType.Int);
    }
    
    @Override
    public Variable Execute(Variable var1, Variable var2) {
        Map<VariableType, VariableType> execute_allowance = this.execute_allowance.get(typeOfOperation);
        VariableType allowed_type = execute_allowance.get(var1.GetVariableType());
        if (allowed_type != null && var2.GetVariableType() == allowed_type) {
            return var2.GetNewVariableSameType();
        }

        String error_str = String.format(
            "operation mul is not possible on %s(%s) and %s(%s)",
            var1.GetVariableName(),
            var1.GetVariableType(),
            var2.GetVariableName(), 
            var2.GetVariableType());
        throw new OperationError(error_str);
    }
}
