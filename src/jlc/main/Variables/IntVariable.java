package jlc.main.Variables;

public class IntVariable implements Variable {
    private String name;
    private VariableKind variableKind;

    public IntVariable(String name) {
        this.name = name;
    }

    public IntVariable(String name, VariableKind variableKind) {
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
        return VariableType.Int;
    }

    @Override
    public void SetVariableType(VariableType type) {
        if (type != VariableType.Int)
            throw new IllegalArgumentException("IntVariable can only have type Int");
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
        return var != null && var.GetVariableType() == VariableType.Int;
    }

    @Override
    public Variable GetNewVariableSameType() {
        return new IntVariable("last", VariableKind.Unkown);
    }
}
