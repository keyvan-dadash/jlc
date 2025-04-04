package javalette;

public class Mul implements Operation {
    Mul() {

    }
    
    @Override
    public Variable Execute(Variable var1, Variable var2) {
        throw new OperationError("operation (?) is not possible on " + var1.GetVariableType() + " and " + var2.GetVariableType());
    }
}
