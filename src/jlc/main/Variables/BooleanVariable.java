package jlc.main.Variables;

public class BooleanVariable implements Variable {
    private String name;
    private VariableKind variableKind;

    public BooleanVariable(String name) {
        this.name = name;
    }

    public BooleanVariable(String name, VariableKind variableKind) {
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
        return VariableType.Boolean;
    }

    @Override
    public void SetVariableType(VariableType type) {
        if (type != VariableType.Boolean)
            throw new IllegalArgumentException("BooleanVariable can only have type Boolean");
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
                return ((ArrayVariable) var).GetArrayType().GetVariableType() == VariableType.Boolean;
            }
        return var.GetVariableType() == VariableType.Boolean;
    }

    @Override
    public Variable GetNewVariableSameType() {
        return new BooleanVariable("last", VariableKind.Unkown);
    }

    @Override
    public Variable GetArrayType() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'GetArrayType'");
    }
}
