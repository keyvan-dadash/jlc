package jlc.main;

import java.util.HashMap;
import java.util.Map;

import jlc.main.Variables.Variable;

public class Ctx {
    
    public Map<String, Variable> ctx_variables;

    public Map<String, Function> functions;

    Exception error;

    public Ctx parent;

    Variable last_expr_result;

    Variable ctx_return_variable;

    public Ctx() {
        ctx_variables = new HashMap<>();
        functions = new HashMap<>();
    }

    public Ctx GetNewChildCtx() {
        Ctx ctx = new Ctx();
        ctx.functions = this.functions;
        ctx.parent = this;
        ctx.ctx_return_variable = this.ctx_return_variable;
        return ctx;
    }
}
