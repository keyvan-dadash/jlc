package javalette;

public class Or implements Operation {
    public Or() {

    }
    
    @Override
    public Variable Execute(Variable var1, Variable var2) {
        throw new OperationError("operation (?) is not possible on " + var1.GetVariableType() + " and " + var2.GetVariableType());
    }
}

