package jlc.main;

import java.util.ArrayList;
import java.util.List;

import jlc.main.Variables.Variable;

// Function class holds necessary information about a function, such as its return value and arguments.
public class Function {
    public Function() {
        func_args = new ArrayList<>();
        func_local = new ArrayList<>();
        func_temps = new ArrayList<>();
    }
    
    public Function(List<Variable> func_args, Variable return_var, String fn_name) {
        this.func_args = func_args;
        this.return_var = return_var;
        this.fn_name = fn_name;
    }

    // CanCall allows to see whether we can call the function with the given list of arguments.
    public boolean CanCall(List<Variable> args) {
        if (args.size() != func_args.size()) {
            throw new RuntimeException(String.format(
                "difference in the number of arguments that has been passed to %s(%d expected) but %d given",
                fn_name,
                func_args.size(),
                args.size())
            );
        }

        for (int i = 0; i < args.size(); i++) {
            if (!func_args.get(i).IsSameAs(args.get(i))) {
                throw new RuntimeException(String.format(
                    "an incompatible argument(%s) has been given to function %s(expected %s)",
                    args.get(i).GetVariableType(),
                    fn_name,
                    func_args.get(i).GetVariableType())
                );
            }
        }

        return true;
    }

    // GetReturn returns the variable type of the function.
    public Variable GetReturn() {
        return return_var;
    }

    // SetFunctionName allows us to set a name for the function.
    public void SetFunctionName(String fn_name) {
        this.fn_name = fn_name;
    }

    // func_args shows what is the type of each of variable of a function's parameter.
    public List<Variable> func_args;

    // func_local holds function's local variable.
    public List<Variable> func_local;

    // temporary variable that resides in this function.
    public List<Variable> func_temps;

    // return_var shows what is the return type of the function.
    public Variable return_var;

    // fn_name holds the name of the function.
    public String fn_name;
}
