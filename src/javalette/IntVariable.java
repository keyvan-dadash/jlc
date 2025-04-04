package javalette;

class IntVariable implements Variable {
    private String name;

    public IntVariable(String name) {
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
        return VariableType.Int;
    }

    @Override
    public void SetVariableType(VariableType type) {
        if (type != VariableType.Int)
            throw new IllegalArgumentException("IntVariable can only have type Int");
    }

    @Override
    public boolean IsConvertableTo(Variable var) {
        if (var == null)
            return false;
        VariableType target = var.GetVariableType();
        return target == VariableType.Int || target == VariableType.Double;
    }

    @Override
    public boolean IsSameAs(Variable var) {
        return var != null && var.GetVariableType() == VariableType.Int;
    }

    @Override
    public Variable GetNewVariableSameType() {
        return new IntVariable("last");
    }
}
