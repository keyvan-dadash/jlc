package jlc.main.Variables;

// Variable interface is a list of common functionality that each variable should implement.
public interface Variable {

    // GetVariableName returns the name of this vairable. For example, i, r.
    String GetVariableName();

    // SetVariableName allows us to set the name of this variable. For example, i, r.
    void SetVariableName(String name);

    // GetVariableType returns the type of this variable, which cloud be boolean, int, double or void.
    VariableType GetVariableType();

    // SetVariableType allows us to set the type of this variable to boolean, int, double or void. 
    void SetVariableType(VariableType type);

    // GetVariableKind returns the kind of this variable, which cloud be FuncParam, FunRet and etc.
    VariableKind GetVariableKind();

    // SetVariableType allows us to set the king of this variable to FunParam, FunRet and etc. 
    void SetVariableKind(VariableKind type);

    // IsSameAs checks if the input variable has the same type of this variable.
    // For example:
    // Variable var1 = new IntVariable();
    // Variable var2 = new BooleanVariable();
    // var1.IsSameAs(var2) == false;
    boolean IsSameAs(Variable var);

    // GetNewVariableSameType returns the type of this variable.
    Variable GetNewVariableSameType();
}