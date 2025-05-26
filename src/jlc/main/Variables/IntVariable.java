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
        if (var == null) return false;
        if (var instanceof ArrayVariable) {
            return ((ArrayVariable) var).GetArrayType().GetVariableType() == VariableType.Int;
        }
        return var.GetVariableType() == VariableType.Int;
    }

    @Override
    public Variable GetNewVariableSameType() {
        return new IntVariable("last", VariableKind.Unkown);
    }

    @Override
    public Variable GetArrayType() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'GetArrayType'");
    }
}
