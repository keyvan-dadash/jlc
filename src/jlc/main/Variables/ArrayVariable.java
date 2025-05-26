package jlc.main.Variables;

public class ArrayVariable implements Variable {
    private String name;
    private VariableKind variableKind;
    private VariableType subtype;

    public ArrayVariable(String name) {
        this.name = name;
    }

    public ArrayVariable(String name, VariableType subtype) {
        this.name = name;
        this.subtype = subtype;
    }

    public ArrayVariable(String name, VariableType subtype, VariableKind kind) {
        this.name = name;
        this.subtype = subtype;
        this.variableKind = kind;
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
        return VariableType.Array;
    }

    @Override
    public void SetVariableType(VariableType type) {
        if (type != VariableType.Array)
            throw new IllegalArgumentException("ArrayVariable can only have type Array");
    }

    @Override
    public VariableKind GetVariableKind() {
        return variableKind;
    }

    public Variable GetArrayType() {
        switch (subtype) {
            case Int:
                return new IntVariable("last");
            case Boolean:
                return new BooleanVariable("last");
            case Double:
                return new DoubleVariable("last");
            default:
                throw new IllegalArgumentException("ArrayVariable does not support this subtype: " + subtype);
        }
    }
    public void SetArrayType(VariableType subtype) {
        this.subtype = subtype;
    }
    @Override
    public void SetVariableKind(VariableKind kind) {
        this.variableKind = kind;
    }
    public VariableType getSubtype() {
        return this.subtype;
    }
    @Override
    public boolean IsSameAs(Variable var) {
        if (var == null) return false;
        // Array-to-array: check subtype
        if (var instanceof ArrayVariable) {
            return this.subtype == ((ArrayVariable) var).subtype;
        }
        // Array <-> element: allow if element type matches
        return this.GetArrayType().IsSameAs(var);
    }
    @Override
    public Variable GetNewVariableSameType() {
        return new ArrayVariable("last");
    }
}

