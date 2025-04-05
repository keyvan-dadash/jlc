package javalette;

import java.util.ArrayList;
import java.util.List;

public class Function {
    public Function() {
        func_args = new ArrayList<>();
    }
    
    public Function(List<Variable> func_args, Variable return_var, String fn_name) {
        this.func_args = func_args;
        this.return_var = return_var;
        this.fn_name = fn_name;
    }

    public boolean CanCall(List<Variable> args) {
        if (args.size() != func_args.size()) {
            throw new RuntimeException("Difference in the number of arguments");
        }

        for (int i = 0; i < args.size(); i++) {
            if (!func_args.get(i).IsSameAs(args.get(i))) {
                throw new RuntimeException("Incompatible argument types");
            }
        }

        return true;
    }

    public Variable GetReturn() {
        return return_var;
    }

    public void SetFunctionName(String fn_name) {
        this.fn_name = fn_name;
    }

    public List<Variable> func_args;
    public Variable return_var;
    public String fn_name;
}
