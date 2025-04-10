package jlc.main.Variables;

// VariableKind shows where does the variable comes from.
// For example, is it a funciton parameter or return of a function.
// This is important for knowing whether we have to allocate a variable,
// or we can use register in our backend.
public enum VariableKind {
    Unkown,
    FuncParam,
    FuncRet,
    LocalVariable,
    GlobalVariable,
    TempVariable,
    ConstantVariable
}
