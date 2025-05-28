package jlc.main.Variables;

public class VoidVariable implements Variable {
    private String name;
    private VariableKind variableKind;

    public VoidVariable(String name) {
        this.name = name;
    }

    public VoidVariable(String name, VariableKind variableKind) {
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
        return VariableType.Void;
    }

    @Override
    public void SetVariableType(VariableType type) {
        if (type != VariableType.Void)
            throw new IllegalArgumentException("VoidVariable can only have type Void");
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
                return ((ArrayVariable) var).GetArrayType().GetVariableType() == VariableType.Void;
            }
        return var.GetVariableType() == VariableType.Void;
    }

    @Override
    public Variable GetNewVariableSameType() {
        return new VoidVariable("last", VariableKind.Unkown);
    }

    @Override
    public Variable GetArrayType() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'GetArrayType'");
    }
}
