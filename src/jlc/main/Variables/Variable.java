package jlc.main.Variables;

public interface Variable {
    String GetVariableName();
    void SetVariableName(String name);
    VariableType GetVariableType();
    void SetVariableType(VariableType type);
    boolean IsConvertableTo(Variable var);
    boolean IsSameAs(Variable var);
    Variable GetNewVariableSameType();
}