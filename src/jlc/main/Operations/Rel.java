package jlc.main.Operations;

import java.util.HashMap;
import java.util.Map;

import jlc.main.Variables.Variable;
import jlc.main.Variables.BooleanVariable;
import jlc.main.Variables.VariableType;

public class Rel implements Operation {
    
    // Map where the key is a RelType and the value is a mapping
    // from allowed left operand VariableType to expected right operand VariableType.
    public Map<RelType, Map<VariableType, VariableType>> execute_allowance;

    public RelType typeOfOperation;

    public Rel(RelType typeOfOperation) {
        execute_allowance = new HashMap<>();
        this.typeOfOperation = typeOfOperation;
        
        // For ordered relational operations, only Int and Double are allowed.
        Map<VariableType, VariableType> orderMap = new HashMap<>();
        orderMap.put(VariableType.Int, VariableType.Int);
        orderMap.put(VariableType.Double, VariableType.Double);
        
        // For equality relational operations, Int, Double, and Boolean are allowed.
        Map<VariableType, VariableType> equalityMap = new HashMap<>();
        equalityMap.put(VariableType.Int, VariableType.Int);
        equalityMap.put(VariableType.Double, VariableType.Double);
        equalityMap.put(VariableType.Boolean, VariableType.Boolean);
        
        // Assign allowed mappings based on the RelType.
        execute_allowance.put(RelType.LTH, orderMap);
        execute_allowance.put(RelType.LE, orderMap);
        execute_allowance.put(RelType.GTH, orderMap);
        execute_allowance.put(RelType.GE, orderMap);
        execute_allowance.put(RelType.EQU, equalityMap);
        execute_allowance.put(RelType.NE, equalityMap);
    }
    
    @Override
    public Variable Execute(Variable var1, Variable var2) {
        Map<VariableType, VariableType> allowedMap = execute_allowance.get(typeOfOperation);
        VariableType allowedType = allowedMap.get(var1.GetVariableType());
        if (allowedType != null && var2.GetVariableType() == allowedType) {
            // Returning a new BooleanVariable as relational operations yield booleans.
            return new BooleanVariable("tmp");
        }
        
        String errorStr = String.format(
            "operation rel is not possible on %s(%s) and %s(%s)",
            var1.GetVariableName(),
            var1.GetVariableType(),
            var2.GetVariableName(), 
            var2.GetVariableType());
        throw new OperationError(errorStr);
    }
}
