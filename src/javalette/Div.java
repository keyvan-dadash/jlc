package javalette;

public abstract class Div implements Operation {
    @Override
    public Variable Execute(Variable var1, Variable var2) {
        throw new OperationError("operation (?) is not possible on " + var1.GetVariableType() + " and " + var2.GetVariableType());
    }
}
