package jlc.main.Variables;

public class StringVariable implements Variable {
    private String name;
    private VariableKind variableKind;

    public StringVariable(String name) {
        this.name = name;
    }

    public StringVariable(String name, VariableKind variableKind) {
        this.name = name;
        this.variableKind = variableKind;
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
    public VariableKind GetVariableKind() {
        return this.variableKind;
    }

    @Override
    public void SetVariableKind(VariableKind type) {
        this.variableKind = type;
    }

    @Override
    public boolean IsSameAs(Variable var) {
        return var != null && var.GetVariableType() == VariableType.String;
    }

    @Override
    public Variable GetNewVariableSameType() {
        return new StringVariable("last", VariableKind.Unkown);
    }
}
