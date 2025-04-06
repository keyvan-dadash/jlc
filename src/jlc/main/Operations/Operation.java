package jlc.main.Operations;

import jlc.main.Variables.Variable;

// Operation is an interface that evey operation of javalette should implement so
// that we could operate of vairables of the langauge regardless of their type of operation.
public interface Operation {

    // Execute will execute operation on the variable var1 and var2, and it will return a result variable.
    // Note: some operation might ignore the second var2. For example, not operation.
    public Variable Execute(Variable var1, Variable var2);
}
