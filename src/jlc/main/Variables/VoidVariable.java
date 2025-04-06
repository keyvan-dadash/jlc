package jlc.main.Variables;

public class VoidVariable implements Variable {
    private String name;

    public VoidVariable(String name) {
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
        return VariableType.Void;
    }

    @Override
    public void SetVariableType(VariableType type) {
        if (type != VariableType.Void)
            throw new IllegalArgumentException("VoidVariable can only have type Void");
    }

    @Override
    public boolean IsConvertableTo(Variable var) {
        return false;
    }

    @Override
    public boolean IsSameAs(Variable var) {
        return var != null && var.GetVariableType() == VariableType.Void;
    }

    @Override
    public Variable GetNewVariableSameType() {
        return new VoidVariable("last");
    }
}
