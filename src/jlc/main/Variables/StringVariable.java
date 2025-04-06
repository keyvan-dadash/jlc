package jlc.main.Variables;

public class StringVariable implements Variable {
    private String name;

    public StringVariable(String name) {
        this.name = name;
    }

    @Override
    public String GetVariableName() {
        return name;
    }

    @Override
    public void SetVariableName(String name) {
        this.name = name;
    }

    @Override
    public VariableType GetVariableType() {
        return VariableType.String;
    }

    @Override
    public void SetVariableType(VariableType type) {
        if (type != VariableType.String)
            throw new IllegalArgumentException("StringVariable can only have type String");
    }

    @Override
    public boolean IsSameAs(Variable var) {
        return var != null && var.GetVariableType() == VariableType.String;
    }

    @Override
    public Variable GetNewVariableSameType() {
        return new StringVariable("last");
    }
}
