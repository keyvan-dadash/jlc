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
    if (var == null) return false;
    if (var instanceof ArrayVariable) {
        return ((ArrayVariable) var).GetArrayType().GetVariableType() == VariableType.String;
    }
    return var.GetVariableType() == VariableType.String;
    }

    @Override
    public Variable GetNewVariableSameType() {
        return new StringVariable("last", VariableKind.Unkown);
    }

    @Override
    public Variable GetArrayType() {
        throw new UnsupportedOperationException("Unimplemented method 'GetArrayType'");
    }
}
