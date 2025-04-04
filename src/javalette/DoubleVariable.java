package javalette;

class DoubleVariable implements Variable {
    private String name;

    public DoubleVariable(String name) {
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
        return VariableType.Double;
    }

    @Override
    public void SetVariableType(VariableType type) {
        if (type != VariableType.Double)
            throw new IllegalArgumentException("DoubleVariable can only have type Double");
    }

    @Override
    public boolean IsConvertableTo(Variable var) {
        if (var == null)
            return false;
        return var.GetVariableType() == VariableType.Double;
    }

    @Override
    public boolean IsSameAs(Variable var) {
        return var != null && var.GetVariableType() == VariableType.Double;
    }

    @Override
    public Variable GetNewVariableSameType() {
        return new DoubleVariable("last");
    }
}
