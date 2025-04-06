package jlc.main;

import java.util.HashMap;
import java.util.Map;

import jlc.main.Operations.Operation;
import jlc.main.Variables.Variable;

// Ctx is a information that we will pass through each block, function or it 
// in order to find out what is going on in the AST.
public class Ctx {

    // ctx_name allows us to give a name to ctx so that make it eaiser for debugging.
    public String ctx_name;
    
    // ctx_variables keeps declared variables of ctx.
    public Map<String, Variable> ctx_variables;

    // functions keeps declared function of ctx.
    public Map<String, Function> functions;

    // parent shows which ctx does this ctx inherit from.
    // This is usefull for the nested block inside a functions.
    // For example, assume we have if inside the ctx and we want to
    // check whether that if has a return statement or not.
    public Ctx parent;

    // last_expr_result keeps result of last expression or anything that has been done.
    // In the end, everything reduces to a variable so we want to keep that.
    Variable last_expr_result;

    // last_operation_to_apply keeps the last parsed operation that need to be done.
    Operation last_operation_to_apply;

    // ctx_return_variable shows what is the type of retrun variable of this ctx.
    Variable ctx_return_variable;

    // is_ctx_return shows whether this ctx gurantees all path to return or not
    boolean is_ctx_return;

    public Ctx() {
        ctx_variables = new HashMap<>();
        functions = new HashMap<>();
        is_ctx_return = false;
    }

    public Ctx(String ctx_name) {
        this.ctx_name = ctx_name;
        ctx_variables = new HashMap<>();
        functions = new HashMap<>();
        is_ctx_return = false;
    }

    // WithVariables takes a ctx as an input and create a child that has same context variables
    // as its parent.
    public static Ctx WithVariables(Ctx parent_ctx, String new_ctx_suffix) {
        Ctx ctx = new Ctx();
        ctx.functions = parent_ctx.functions;
        ctx.parent = parent_ctx;
        ctx.ctx_return_variable = parent_ctx.ctx_return_variable;
        ctx.ctx_variables = parent_ctx.ctx_variables;
        ctx.ctx_name = String.format("%s.%s", parent_ctx.ctx_name, new_ctx_suffix);
        return ctx;
    }

    // WithoutVariables takes a ctx as an input and create a child, but it does
    // not inherit its parent's variables. This usefull for the nested block inside a function
    // when we want to support shadowing variables.
    public static Ctx WithoutVariables(Ctx parent_ctx, String new_ctx_suffix) {
        Ctx ctx = new Ctx();
        ctx.functions = parent_ctx.functions;
        ctx.parent = parent_ctx;
        ctx.ctx_return_variable = parent_ctx.ctx_return_variable;
        ctx.ctx_variables.clear();;
        ctx.ctx_name = String.format("%s.%s", parent_ctx.ctx_name, new_ctx_suffix);
        return ctx;
    }

    // WithoutParentAndVariables takes a ctx as an input and create a child, but it does
    // not inherit its parent's variables. This usefull for the nested block inside a function
    // when we want to support shadowing variables. Moreover, this function does not assign 
    // the new ctx's parent since we dont want to explore the parent ctx in some cases.
    public static Ctx WithoutParentAndVariables(Ctx parent_ctx, String new_ctx_suffix) {
        Ctx ctx = new Ctx();
        ctx.functions = parent_ctx.functions;
        ctx.parent = null;
        ctx.ctx_return_variable = parent_ctx.ctx_return_variable;
        ctx.ctx_variables.clear();;
        ctx.ctx_name = String.format("%s.%s", parent_ctx.ctx_name, new_ctx_suffix);
        return ctx;
    }
}
