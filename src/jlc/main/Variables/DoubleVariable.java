package jlc.main.Variables;

public class DoubleVariable implements Variable {
    private String name;
    private VariableKind variableKind;

    public DoubleVariable(String name) {
        this.name = name;
    }

    public DoubleVariable(String name, VariableKind variableKind) {
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
        return VariableType.Double;
    }

    @Override
    public void SetVariableType(VariableType type) {
        if (type != VariableType.Double)
            throw new IllegalArgumentException("DoubleVariable can only have type Double");
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
        return var != null && var.GetVariableType() == VariableType.Double;
    }

    @Override
    public Variable GetNewVariableSameType() {
        return new DoubleVariable("last", variableKind);
    }
}
