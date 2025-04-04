package javalette;

class BooleanVariable implements Variable {
    private String name;

    public BooleanVariable(String name) {
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
        return VariableType.Boolean;
    }

    @Override
    public void SetVariableType(VariableType type) {
        if (type != VariableType.Boolean)
            throw new IllegalArgumentException("BooleanVariable can only have type Boolean");
    }

    @Override
    public boolean IsConvertableTo(Variable var) {
        if (var == null)
            return false;
        return var.GetVariableType() == VariableType.Boolean;
    }

    @Override
    public boolean IsSameAs(Variable var) {
        return var != null && var.GetVariableType() == VariableType.Boolean;
    }

    @Override
    public Variable GetNewVariableSameType() {
        return new BooleanVariable("last");
    }
}
