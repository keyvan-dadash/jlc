package jlc.main;

import java.security.spec.ECFieldF2m;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import jlc.lib.javalette.Absyn.ArrAss;
import jlc.lib.javalette.Absyn.ArrAssExpr;
import jlc.lib.javalette.Absyn.ArrDecr;
import jlc.lib.javalette.Absyn.ArrIncr;
import jlc.lib.javalette.Absyn.ArrIndex;
import jlc.lib.javalette.Absyn.ArrLen;
import jlc.lib.javalette.Absyn.ArrType;
import jlc.lib.javalette.Absyn.Bool;
import jlc.lib.javalette.Absyn.Doub;
import jlc.lib.javalette.Absyn.EVar;
import jlc.lib.javalette.Absyn.ForEach;
import jlc.lib.javalette.Absyn.Fun;
import jlc.lib.javalette.Absyn.Int;
import jlc.lib.javalette.Absyn.NewArr;
import jlc.lib.javalette.Absyn.TypeBase;
import jlc.lib.javalette.Absyn.Void;
import jlc.main.Operations.Add;
import jlc.main.Operations.AddType;
import jlc.main.Operations.And;
import jlc.main.Operations.Ass;
import jlc.main.Operations.Mul;
import jlc.main.Operations.MulType;
import jlc.main.Operations.Neg;
import jlc.main.Operations.Not;
import jlc.main.Operations.Operation;
import jlc.main.Operations.Or;
import jlc.main.Operations.Rel;
import jlc.main.Operations.RelType;
import jlc.main.Variables.Variable;
import jlc.main.Variables.IntVariable;
import jlc.main.Variables.StringVariable;
import jlc.main.Variables.VoidVariable;
import jlc.main.Variables.ArrayVariable;
import jlc.main.Variables.BooleanVariable;
import jlc.main.Variables.DoubleVariable;

public class TypeCheckerVisit {
    public static final Logger logger = Logger.getLogger(TypeCheckerVisit.class.getName());

    // GetVariableFromCtxOrPreviousCtx tries to access a variable form this ctx or previous ctx
    public Variable GetVariableFromCtxOrPreviousCtx(Ctx ctx, String identifier) {
        while (true) {
            Variable var = ctx.ctx_variables.get(identifier);
            if (var == null) {
                ctx = ctx.parent;
            if (ctx == null) {
                return null;
            }
            } else {
                return var;
            }
        }
    }
    public class ProgVisitor implements jlc.lib.javalette.Absyn.Prog.Visitor<Ctx, Ctx> {
        public Ctx visit(jlc.lib.javalette.Absyn.Program p, Ctx ctx) {
          ctx = this.ctx;
          
          for (jlc.lib.javalette.Absyn.TopDef x: p.listtopdef_) {
            ctx = x.accept(new TopDefVisitor(), ctx);
          }
    
          return ctx;
        }

        public Ctx ctx;

        // SetCtx will set a ctx that vistor should use.
        public void SetCtx(Ctx ctx) {
            this.ctx = ctx;
            ctx.ctx_variables.clear();
        }
      }
    
      public class TopDefVisitor implements jlc.lib.javalette.Absyn.TopDef.Visitor<Ctx, Ctx> {
        public Ctx visit(jlc.lib.javalette.Absyn.FnDef p, Ctx ctx) {

          ctx = p.type_.accept(new TypeVisitor(), ctx);
          ctx.last_expr_result = null;

          for (jlc.lib.javalette.Absyn.Arg x: p.listarg_) {
            ctx = x.accept(new ArgVisitor(), ctx);
            ctx.last_expr_result = null;
          }

          // process the function with a temporary ctx.
          Function fn = ctx.functions.get(p.ident_);
          Ctx tmp_ctx = Ctx.WithoutParentAndVariables(ctx, fn.fn_name);

          // We should put function's argument as the variable of new ctx since the application
          // start to access them.
          for (Variable arg : fn.func_args) {
            logger.finest(String.format(
                "putting function %s argument %s(%s) inside the ctx variables", 
                fn.fn_name,
                arg.GetVariableName(),
                arg.GetVariableType()));
            tmp_ctx.ctx_variables.put(arg.GetVariableName(), arg);
          }

          // Whenever the temprary ctx hit a return statement should return as the function return type.
          tmp_ctx.ctx_return_variable = fn.return_var;
          tmp_ctx = p.blk_.accept(new BlkVisitor(), tmp_ctx);

          // Check whether the function (ctx) does have a gurantee to return
          // Also ignore if the return type of the function is void since we dont write return for void
          if (!tmp_ctx.is_ctx_return && !tmp_ctx.ctx_return_variable.IsSameAs(new VoidVariable(""))) {
            throw new RuntimeException(String.format("function %s does not return", fn.fn_name));
          }

          return ctx;
        }
      }
    
      public class ArgVisitor implements jlc.lib.javalette.Absyn.Arg.Visitor<Ctx, Ctx> {
        public Ctx visit(jlc.lib.javalette.Absyn.Argument p, Ctx ctx) {
          ctx = p.type_.accept(new TypeVisitor(), ctx);
          Variable last = ctx.last_expr_result;
          last.SetVariableName(p.ident_);
          return ctx;
        }
      }
    
      public class BlkVisitor implements jlc.lib.javalette.Absyn.Blk.Visitor<Ctx, Ctx> {
        public Ctx visit(jlc.lib.javalette.Absyn.Block p, Ctx ctx) {
          logger.finest(String.format("entered the a new block(ctx: %s)", ctx.ctx_name));
          for (jlc.lib.javalette.Absyn.Stmt x: p.liststmt_) {
            ctx = x.accept(new StmtVisitor(), ctx);
          }

          return ctx;
        }
      }
    
      public class StmtVisitor implements jlc.lib.javalette.Absyn.Stmt.Visitor<Ctx, Ctx> {
        public Ctx visit(ArrAssExpr p, Ctx ctx) {
          ctx.last_expr_result = null;
          // Evaluate the array variable
          ctx = p.expr_1.accept(new ExprVisitor(), ctx);
          Variable left_side_var = ctx.last_expr_result;

          if (!(left_side_var instanceof ArrayVariable)) {
            throw new RuntimeException(String.format("In ArrAssExpr: Variable: %s is not an array", left_side_var));
          }
          
          // Evaluate the index
          ctx.last_expr_result = null;
          ctx = p.expr_2.accept(new ExprVisitor(), ctx);
          Variable indexVar = ctx.last_expr_result;
          if (!(indexVar instanceof IntVariable)) {
            throw new RuntimeException("Array index must be an integer");
          }

          // Evaluate the value to assign
          ctx.last_expr_result = null;
          ctx = p.expr_3.accept(new ExprVisitor(), ctx);
          Variable valueVar = ctx.last_expr_result;
          if (!left_side_var.GetArrayType().IsSameAs(valueVar)) {
            throw new RuntimeException("Value type does not match array type");
          }
          // Now we have the array variable, index and value to assign.
          Operation op = new Ass();
          op.Execute(left_side_var.GetArrayType(), valueVar);


          return ctx;
        }

        public Ctx visit(ArrIncr p, Ctx ctx) {
          //Check that the array index is an int
          ctx.last_expr_result = null;
          ctx = p.expr_.accept(new ExprVisitor(), ctx);
          Variable indexVar = ctx.last_expr_result;
          if (!(indexVar instanceof IntVariable)) {
            throw new RuntimeException("Variable index is not an integer");
          }
          // Check that identity is an array variable
          Variable left_side_var = GetVariableFromCtxOrPreviousCtx(ctx, p.ident_);
          if (left_side_var == null) {
            throw new RuntimeException(String.format("In ArrIncr: variable %s has not be decelared before", p.ident_));
          }
          if (!(left_side_var instanceof ArrayVariable)) {
            throw new RuntimeException(String.format("variable %s is not an array", p.ident_));
          }

          Operation add = new Add(AddType.Minus);
          
          Variable one = new IntVariable("1");

          add.Execute(left_side_var.GetArrayType() , one);

          return ctx;
        }

        public Ctx visit(ArrDecr p, Ctx ctx) {
          //Check that the array index is an int
          ctx.last_expr_result = null;
          ctx = p.expr_.accept(new ExprVisitor(), ctx);
          Variable indexVar = ctx.last_expr_result;
          if (!(indexVar instanceof IntVariable)) {
            throw new RuntimeException("Variable index is not an integer");
          }
          // Check that identity is an array variable
          Variable left_side_var = GetVariableFromCtxOrPreviousCtx(ctx, p.ident_);
          if (left_side_var == null) {
            throw new RuntimeException(String.format("In ArrDecr: variable %s has not be decelared before", p.ident_));
          }
          if (!(left_side_var instanceof ArrayVariable)) {
            throw new RuntimeException(String.format("variable %s is not an array", p.ident_));
          }

          Operation add = new Add(AddType.Plus);
          
          Variable one = new IntVariable("1");

          add.Execute(left_side_var.GetArrayType() , one);

          return ctx;
        }

        public Ctx visit(ArrAss p, Ctx ctx) {
          // Evaluate the array variable
          Variable left_side_var = GetVariableFromCtxOrPreviousCtx(ctx, p.ident_);
          if (!(left_side_var instanceof ArrayVariable)) {
            throw new RuntimeException(String.format("In ArrAss: Variable: %s is not an array", left_side_var));
          }
          ctx.last_expr_result = null;
          // Evaluate the index
          ctx = p.expr_1.accept(new ExprVisitor(), ctx);
          Variable indexVar = ctx.last_expr_result;

          if (!(indexVar instanceof IntVariable)) {
            throw new RuntimeException(String.format("In ArrAss: Array index %s must be an integer", indexVar));
          }
          ctx.last_expr_result = null;

          // Evaluate the value to assign
          ctx = p.expr_2.accept(new ExprVisitor(), ctx);
          Variable valueVar = ctx.last_expr_result;
          if (!left_side_var.GetArrayType().IsSameAs(valueVar)) {
            throw new RuntimeException("Value type does not match array type");
          }

          // Now we have the array variable, index and value to assign.
          Operation op = new Ass();
          // throw new RuntimeException(String.format("In ArrAss: Left side var %s, and value to assign: %s", left_side_var, valueVar));
          Variable return_var = op.Execute(left_side_var.GetArrayType(), valueVar); 

          return ctx;
        }

        public Ctx visit(ForEach p, Ctx ctx) {
          //Check the type
          ctx.last_expr_result = null;
          ctx = p.type_.accept(new TypeVisitor(), ctx);
          Variable typeVar = ctx.last_expr_result;
          
          //Check that var has not been declared before
          if (ctx.ctx_variables.containsKey(p.ident_)) {
              throw new RuntimeException(String.format("Identity: %s is already in use in this scope", p.ident_));
          }

          // Check that expr evaluates to an array
          ctx.last_expr_result = null;
          ctx = p.expr_.accept(new ExprVisitor(), ctx);
          Variable arrVar = ctx.last_expr_result;
          if (!(arrVar instanceof ArrayVariable)) {
            throw new RuntimeException("Expression is not an array");
          }

          // Check that the type of the array matches the type of the loop variable
          if (!arrVar.GetArrayType().IsSameAs(typeVar)) {
            throw new RuntimeException(String.format("Array type %s does not match loop variable type %s", arrVar.GetArrayType(), typeVar));
          }

          // Create a new variable for the loop variable
          Variable loopVar = typeVar.GetNewVariableSameType();
          loopVar.SetVariableName(p.ident_);
          loopVar.SetVariableKind(typeVar.GetVariableKind());

          // Create a new child context for the loop variable
          Ctx loopCtx = Ctx.WithoutVariables(ctx, "forEach"); // inherits parent variables, but new variable map
          loopCtx.ctx_variables.put(loopVar.GetVariableName(), loopVar);

          // Visit the loop body with the new context
          loopCtx = p.stmt_.accept(new StmtVisitor(), loopCtx);

          return ctx;
        }

        public Ctx visit(jlc.lib.javalette.Absyn.Empty p, Ctx ctx) {
          return ctx;
        }

        public Ctx visit(jlc.lib.javalette.Absyn.BStmt p, Ctx ctx) {
          // New block so we should create new ctx. We simply name it's child ctx as block.
          Ctx tmpCtx = Ctx.WithoutVariables(ctx, "block");
          tmpCtx = p.blk_.accept(new BlkVisitor(), tmpCtx);

          // Set that the block whether returns or not
          ctx.is_ctx_return = tmpCtx.is_ctx_return;
          return ctx;
        }

        public Ctx visit(jlc.lib.javalette.Absyn.Decl p, Ctx ctx) {
          p.type_.accept(new TypeVisitor(), ctx);
          // Here we have the result of type visitor in the ctx.last_expr_result
          // We should use this in order to verify that a variable did not define
          // with the type of void
          Variable typeOfVar = ctx.last_expr_result;
          if (typeOfVar.IsSameAs(new VoidVariable(""))) {
            // TODO: bug of print
            throw new RuntimeException(String.format("vairable %s has been declared with the type of void", typeOfVar.GetVariableName()));
          }

          for (jlc.lib.javalette.Absyn.Item x: p.listitem_) {
            ctx = x.accept(new ItemVisitor(), ctx);
            Variable var = ctx.last_expr_result;

            // Lets check if this variable has been declared before.
            if (ctx.ctx_variables.containsKey(var.GetVariableName())) {
                throw new RuntimeException(String.format("vairable %s has been declared in this scope", var.GetVariableName()));
            }

            ctx.ctx_variables.put(var.GetVariableName(), var);

            // put the a variable with the same type so that the next iteration of this loop can understand 
            // the type of its vairable suppose to make.
            ctx.last_expr_result = var.GetNewVariableSameType();
          }

          return ctx;
        }

        public Ctx visit(jlc.lib.javalette.Absyn.Ass p, Ctx ctx) {
          // Clear the varirables tmp
          ctx.last_expr_result = null;
          ctx = p.expr_.accept(new ExprVisitor(), ctx);

          // What is the result of experesion?
          Variable var = ctx.last_expr_result;
          Variable left_side_var = GetVariableFromCtxOrPreviousCtx(ctx, p.ident_);

          // Variable does not exist
          if (left_side_var == null) {
            throw new RuntimeException(String.format("In Ass: variable %s has not be decelared before", left_side_var.GetVariableName()));
          }

          Operation op = new Ass();

          // The return does not matter since we dont care about the return of assignment operation.
          Variable _return_var = op.Execute(left_side_var, var);

          return ctx;
        }

        public Ctx visit(jlc.lib.javalette.Absyn.Incr p, Ctx ctx) {
          // This is same as add with one
          ctx.last_expr_result = null;
          ctx = p.expr_.accept(new ExprVisitor(), ctx);
          Variable var = ctx.last_expr_result;

          Operation add = new Add(AddType.Plus);
          
          Variable one = new IntVariable("temp");
          add.Execute(var, one);

          return ctx;
        }

        public Ctx visit(jlc.lib.javalette.Absyn.Decr p, Ctx ctx) {
          // This is same as add with one
          ctx.last_expr_result = null;
          ctx = p.expr_.accept(new ExprVisitor(), ctx);
          Variable var = ctx.last_expr_result;

          Operation min = new Add(AddType.Minus);
          
          Variable one = new IntVariable("temp");
          min.Execute(var, one);
          
          return ctx;
        }

        public Ctx visit(jlc.lib.javalette.Absyn.Ret p, Ctx ctx) {
          // Clear the last expression so that new expression can start and gives of the return value of it.
          ctx.last_expr_result = null;
          ctx = p.expr_.accept(new ExprVisitor(), ctx);

          Variable exper_result = ctx.last_expr_result;
          if (!ctx.ctx_return_variable.IsSameAs(exper_result)) {
            throw new RuntimeException(String.format(
                "return type %s expected, but %s has been returned",
                ctx.ctx_return_variable.GetVariableType(),
                exper_result.GetVariableType()));
          }

          // Let's clear the last_exper_result so that the rest of the tree can go on.
          ctx.last_expr_result = null;

          // We hit return so this ctx has a return
          ctx.is_ctx_return = true;
          return ctx;
        }

        public Ctx visit(jlc.lib.javalette.Absyn.VRet p, Ctx ctx) {
          if (!ctx.ctx_return_variable.IsSameAs(new VoidVariable("tmp"))) {
            throw new RuntimeException(String.format(
                "return type %s expected, but void has been returned",
                ctx.ctx_return_variable.GetVariableType()));
          }

          // Let's clear the last_exper_result so that the rest of the tree can go on.
          ctx.last_expr_result = null;

          // We hit return so this ctx has a return
          ctx.is_ctx_return = true;
          return ctx;
        }

        public Ctx visit(jlc.lib.javalette.Absyn.Cond p, Ctx ctx) {
          // Clear the last expression so that new expression can start and gives of the return value of it.
          ctx.last_expr_result = null;
          ctx = p.expr_.accept(new ExprVisitor(), ctx);
          Variable exper_result = ctx.last_expr_result;
          ctx.last_expr_result = null;
          if (!exper_result.IsSameAs(new BooleanVariable("tmp"))) {
            throw new RuntimeException(String.format("condition's result should boolean(ctx: %s)", ctx.ctx_name));
          }

          Ctx tmpCtx = Ctx.WithVariables(ctx, "ifSmt");
          tmpCtx = p.stmt_.accept(new StmtVisitor(), tmpCtx);

          // We are not going to set return of that tmp ctx as return the whole function ctx
          // since this is single if. In if and else, we are setting the return of the temporar ctx to whole ctx.
          return ctx;
        }

        public Ctx visit(jlc.lib.javalette.Absyn.CondElse p, Ctx ctx) {
          
          // Clear the varirables tmp
          ctx.last_expr_result = null;
          ctx = p.expr_.accept(new ExprVisitor(), ctx);
          Variable exper_result = ctx.last_expr_result;
          if (!exper_result.IsSameAs(new BooleanVariable("tmp"))) {
            throw new RuntimeException(String.format("condition's result should boolean(ctx: %s)", ctx.ctx_name));
          }

          ctx.last_expr_result = null;
          Ctx tmpCtx = Ctx.WithVariables(ctx, "ifSmt");
          tmpCtx = p.stmt_1.accept(new StmtVisitor(), tmpCtx);

          ctx.last_expr_result = null;
          Ctx tmpCtx1 = Ctx.WithVariables(ctx, "ifSmt");
          tmpCtx1 = p.stmt_2.accept(new StmtVisitor(), tmpCtx1);

          // Both statement of the if should return so that we say the whole ctx returns.
          if (!ctx.is_ctx_return) {
            ctx.is_ctx_return = tmpCtx.is_ctx_return && tmpCtx1.is_ctx_return;
          }

          return ctx;
        }

        public Ctx visit(jlc.lib.javalette.Absyn.While p, Ctx ctx) {
          
          // Clear the varirables tmp
          ctx.last_expr_result = null;

          ctx = p.expr_.accept(new ExprVisitor(), ctx);
          Variable exper_result = ctx.last_expr_result;
          ctx.last_expr_result = null;
          if (!exper_result.IsSameAs(new BooleanVariable("tmp"))) {
            throw new RuntimeException(String.format("condition's result should boolean(ctx: %s)", ctx.ctx_name));
          }

          Ctx tmpCtx = Ctx.WithVariables(ctx, "ifSmt");
          tmpCtx = p.stmt_.accept(new StmtVisitor(), tmpCtx);

          // We are not going to set return of that tmp ctx as return the whole function ctx
          // since this is single if. In if and else, we are setting the return of the temporar ctx to whole ctx.
          return ctx;
        }

        public Ctx visit(jlc.lib.javalette.Absyn.SExp p, Ctx ctx) {
          // Clear the varirables tmp
          ctx.last_expr_result = null;
          ctx = p.expr_.accept(new ExprVisitor(), ctx);

          // This is an edge case for handling test case bad063
          if (ctx.last_expr_result != null && !ctx.last_expr_result.IsSameAs(new VoidVariable(""))) {
            throw new RuntimeException("the result of expression should be void");
          }

          return ctx;
        }
      }
    
      public class ItemVisitor implements jlc.lib.javalette.Absyn.Item.Visitor<Ctx, Ctx> {
        public Ctx visit(jlc.lib.javalette.Absyn.NoInit p, Ctx ctx) {
            Variable var = ctx.last_expr_result;
            var.SetVariableName(p.ident_);
          return ctx;
        }

        public Ctx visit(jlc.lib.javalette.Absyn.Init p, Ctx ctx) {
          Variable var = ctx.last_expr_result;
          // Clear the varirables tmp
          ctx.last_expr_result = null;
          ctx = p.expr_.accept(new ExprVisitor(), ctx);
          Variable exper_result = ctx.last_expr_result;
          if (!var.IsSameAs(exper_result)) {
            throw new RuntimeException(String.format("Result of %s is incompatible with variable type: %s", exper_result, var));
          }
          exper_result.SetVariableName(p.ident_);

          return ctx;
        }
      }
    
      public class TypeVisitor implements jlc.lib.javalette.Absyn.Type.Visitor<Ctx, Ctx> {
        
        public Ctx visit(jlc.lib.javalette.Absyn.Fun p, Ctx ctx) {
          // TODO: I dont have any idea what is this
          ctx = p.type_.accept(new TypeVisitor(), ctx);
          for (jlc.lib.javalette.Absyn.Type x: p.listtype_) {
            ctx = x.accept(new TypeVisitor(), ctx);
          }
          return ctx;
        }
        public Ctx visit(ArrType p, Ctx ctx) {
          ctx.last_expr_result = null;
          ctx = p.type_.accept(new TypeVisitor(), ctx);
          Variable arrayType = ctx.last_expr_result;
          if (!(arrayType instanceof Variable)) {
            throw new RuntimeException("Array type should have a base type");
          }
          // Create a new array variable with the base type
          Variable arrayVar = new ArrayVariable("tmp_array_variable", arrayType.GetVariableType());
          arrayVar.SetVariableKind(arrayType.GetVariableKind());
          ctx.last_expr_result = arrayVar;
          return ctx;
        }

        public Ctx visit(jlc.lib.javalette.Absyn.TypeBase p, Ctx ctx) {
          ctx = p.basetype_.accept(new BaseTypeVisitor(), ctx);
          return ctx;
        }
      }
      public class BaseTypeVisitor implements jlc.lib.javalette.Absyn.BaseType.Visitor<Ctx, Ctx> {        
        public Ctx visit(Int p, Ctx ctx) {
          ctx.last_expr_result = new IntVariable("last");
          return ctx;
        }
        
        public Ctx visit(Doub p, Ctx ctx) {
          ctx.last_expr_result = new DoubleVariable("last");
          return ctx;
        }
        
        public Ctx visit(Bool p, Ctx ctx) {
          ctx.last_expr_result = new BooleanVariable("last");
          return ctx;
        }
        
        public Ctx visit(Void p, Ctx ctx) {
          ctx.last_expr_result = new VoidVariable("last");
          return ctx;
        }

      }

      public class ExprVisitor implements jlc.lib.javalette.Absyn.Expr.Visitor<Ctx, Ctx> {
        public Ctx visit(NewArr p, Ctx ctx) {

          // Evaluate that the index is an integer
          ctx = p.expr_.accept(new ExprVisitor(), ctx);
          Variable indexVar = ctx.last_expr_result;
          if (!(indexVar instanceof IntVariable)) {
            throw new RuntimeException("Array index must be an integer");
          }
          ctx.last_expr_result = null;
          // Evaluate the type of the array
          ctx = p.basetype_.accept(new BaseTypeVisitor(), ctx);
          Variable baseTypeVar = ctx.last_expr_result;
          if (!(baseTypeVar instanceof Variable)) {
            throw new RuntimeException("Array type should have a base type");
          }
          // Create a new array variable with the base type
          Variable arrayVar = new ArrayVariable("tmp_array_variable", baseTypeVar.GetVariableType());
          ctx.last_expr_result = arrayVar;

          return ctx;
        }

        public Ctx visit(ArrLen p, Ctx ctx) {

          // Evaluate array variable
          ctx = p.expr_.accept(new ExprVisitor(), ctx);
          Variable arrayVar = ctx.last_expr_result;
          ctx.last_expr_result = null;

          if (!(arrayVar instanceof ArrayVariable)) {
            throw new RuntimeException(String.format("In ArrLen: Variable: %s is not an array", arrayVar));
          }
          // Check that identity is equal to string literal "length"
          if (!(p.ident_ == "length")){
            throw new RuntimeException(String.format("In ArrLen: Variable: %s is not length", p.ident_));
          }


          // Set the return type of this expression to be integer
          ctx.last_expr_result = new IntVariable("tmp_int_variable");

          return ctx;
        }
        public Ctx visit(ArrIndex p, Ctx ctx) {
          // Evaluate index
          ctx.last_expr_result = null;
          ctx = p.expr_2.accept(new ExprVisitor(), ctx);
          Variable indexVar = ctx.last_expr_result;

          if (!(indexVar instanceof IntVariable)) {
            throw new RuntimeException("Array index must be an integer");
          }

          // Evaluate array variable
          ctx.last_expr_result = null;
          ctx = p.expr_1.accept(new ExprVisitor(), ctx);
          Variable arrayVar = ctx.last_expr_result;

          if (!(arrayVar instanceof ArrayVariable)) {
            throw new RuntimeException(String.format("In ArrIndex: Variable: %s is not an array", arrayVar));
          }

          ctx.last_expr_result = arrayVar.GetArrayType();
          return ctx;
        }

        public Ctx visit(jlc.lib.javalette.Absyn.ELitInt p, Ctx ctx) {
          Random rand = new Random();
          Variable var = new IntVariable("tmp_int_variable" + rand.nextInt(100000));
          if (ctx.last_expr_result != null) {
            throw new RuntimeException("last element exist!");
          }

          ctx.last_expr_result = var;
          return ctx;
        }

        public Ctx visit(jlc.lib.javalette.Absyn.ELitDoub p, Ctx ctx) {
          Random rand = new Random();
          Variable var = new DoubleVariable("tmp_double_variable" + rand.nextInt(100000));
          if (ctx.last_expr_result != null) {
            throw new RuntimeException("last element exist!");
          }

          ctx.last_expr_result = var;
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.ELitTrue p, Ctx ctx) {
            Random rand = new Random();
            Variable var = new BooleanVariable("tmp_bool_variable" + rand.nextInt(100000));
            if (ctx.last_expr_result != null) {
              throw new RuntimeException("last element exist!");
            }
  
            ctx.last_expr_result = var;
            return ctx;
        }

        public Ctx visit(jlc.lib.javalette.Absyn.ELitFalse p, Ctx ctx) {
            Random rand = new Random();
            Variable var = new BooleanVariable("tmp_bool_variable" + rand.nextInt(100000));
            if (ctx.last_expr_result != null) {
              throw new RuntimeException("last element exist!");
            }
  
            ctx.last_expr_result = var;
            return ctx;
        }

        public Ctx visit(jlc.lib.javalette.Absyn.EApp p, Ctx ctx) {
          Function fn = ctx.functions.get(p.ident_);
          if (fn == null) {
            throw new RuntimeException("the function " + p.ident_ + " does not exist!");
          }

          List<Variable> args = new ArrayList<>();

          for (jlc.lib.javalette.Absyn.Expr x: p.listexpr_) {
            ctx = x.accept(new ExprVisitor(), ctx);

            // Construct args
            Variable var = ctx.last_expr_result;
            args.add(var);
            ctx.last_expr_result = null;
          }

          boolean canCall = fn.CanCall(args);
          if (!canCall) {
            throw new RuntimeException("cannot call function " + p.ident_ + " with the arguments!");
          }

          ctx.last_expr_result = fn.GetReturn();

          return ctx;
        }

        public Ctx visit(jlc.lib.javalette.Absyn.EString p, Ctx ctx) {
          Random rand = new Random();
          Variable var = new StringVariable("tmp_string_variable" + rand.nextInt(100000));
          if (ctx.last_expr_result != null) {
            throw new RuntimeException("last element exist!");
          }

          ctx.last_expr_result = var;
          return ctx;
        }

        public Ctx visit(jlc.lib.javalette.Absyn.Neg p, Ctx ctx) {
          ctx = p.expr_.accept(new ExprVisitor(), ctx);
          Variable var = ctx.last_expr_result;
          Operation op = new Neg();

          var = op.Execute(var, new VoidVariable(""));
          ctx.last_expr_result = null;
          ctx.last_expr_result = var;

          return ctx;
        }

        public Ctx visit(jlc.lib.javalette.Absyn.Not p, Ctx ctx) {
          ctx = p.expr_.accept(new ExprVisitor(), ctx);
          Variable var = ctx.last_expr_result;
          Operation op = new Not();

          var = op.Execute(var, new VoidVariable(""));
          ctx.last_expr_result = null;
          ctx.last_expr_result = var;
          
          return ctx;
        }

        public Ctx visit(jlc.lib.javalette.Absyn.EMul p, Ctx ctx) {
          ctx = p.expr_1.accept(new ExprVisitor(), ctx);
          Variable var1 = ctx.last_expr_result;
          ctx.last_expr_result = null;

          ctx = p.mulop_.accept(new MulOpVisitor(), ctx);
          Operation op = ctx.last_operation_to_apply;
          ctx.last_operation_to_apply = null;

          ctx = p.expr_2.accept(new ExprVisitor(), ctx);
          Variable var2 = ctx.last_expr_result;
          ctx.last_expr_result = null;

          Variable result = op.Execute(var1, var2);
          ctx.last_expr_result = result;

          return ctx;
        }

        public Ctx visit(jlc.lib.javalette.Absyn.EAdd p, Ctx ctx) {
            ctx = p.expr_1.accept(new ExprVisitor(), ctx);
            Variable var1 = ctx.last_expr_result;
            ctx.last_expr_result = null;
  
            ctx = p.addop_.accept(new AddOpVisitor(), ctx);
            Operation op = ctx.last_operation_to_apply;
            ctx.last_operation_to_apply = null;
  
            ctx = p.expr_2.accept(new ExprVisitor(), ctx);
            Variable var2 = ctx.last_expr_result;
            ctx.last_expr_result = null;
  
            Variable result = op.Execute(var1, var2);
            ctx.last_expr_result = result;
  
            return ctx;
        }

        public Ctx visit(jlc.lib.javalette.Absyn.ERel p, Ctx ctx) {
          
            ctx = p.expr_1.accept(new ExprVisitor(), ctx);
            Variable var1 = ctx.last_expr_result;
            ctx.last_expr_result = null;

            ctx = p.relop_.accept(new RelOpVisitor(), ctx);
            Operation op = ctx.last_operation_to_apply;
            ctx.last_operation_to_apply = null;

            ctx = p.expr_2.accept(new ExprVisitor(), ctx);
            Variable var2 = ctx.last_expr_result;
            ctx.last_expr_result = null;

            Variable result = op.Execute(var1, var2);
            ctx.last_expr_result = result;

          return ctx;
        }

        public Ctx visit(jlc.lib.javalette.Absyn.EAnd p, Ctx ctx) {
          ctx = p.expr_1.accept(new ExprVisitor(), ctx);
          Variable var1 = ctx.last_expr_result;
          ctx.last_expr_result = null;

          Operation op = new And();

          ctx = p.expr_2.accept(new ExprVisitor(), ctx);
          Variable var2 = ctx.last_expr_result;
          ctx.last_expr_result = null;

          Variable result = op.Execute(var1, var2);
          ctx.last_expr_result = result;

          return ctx;
        }

        public Ctx visit(jlc.lib.javalette.Absyn.EOr p, Ctx ctx) {
          ctx = p.expr_1.accept(new ExprVisitor(), ctx);
          Variable var1 = ctx.last_expr_result;
          ctx.last_expr_result = null;

          Operation op = new Or();

          ctx = p.expr_2.accept(new ExprVisitor(), ctx);
          Variable var2 = ctx.last_expr_result;
          ctx.last_expr_result = null;

          Variable result = op.Execute(var1, var2);
          ctx.last_expr_result = result;
          
          return ctx;
        }

        public Ctx visit(EVar p, Ctx arg) {
          // Get the variable from the ctx or previous ctx
          Variable var = GetVariableFromCtxOrPreviousCtx(arg, p.ident_);
          if (var == null) {
            throw new RuntimeException(String.format("In EVar: variable %s has not be decelared before", p.ident_));
          }

          // Set the last expression result to be this variable
          arg.last_expr_result = var;
          return arg;
        }

      }
    
      public class AddOpVisitor implements jlc.lib.javalette.Absyn.AddOp.Visitor<Ctx, Ctx> {
        public Ctx visit(jlc.lib.javalette.Absyn.Plus p, Ctx ctx) {
          ctx.last_operation_to_apply = new Add(AddType.Plus);
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.Minus p, Ctx ctx) {
          ctx.last_operation_to_apply = new Add(AddType.Minus);
          return ctx;
        }
      }
    
      public class MulOpVisitor implements jlc.lib.javalette.Absyn.MulOp.Visitor<Ctx, Ctx> {
        public Ctx visit(jlc.lib.javalette.Absyn.Times p, Ctx ctx) {
          ctx.last_operation_to_apply = new Mul(MulType.Times);
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.Div p, Ctx ctx) {
          ctx.last_operation_to_apply = new Mul(MulType.Div);
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.Mod p, Ctx ctx) {
          ctx.last_operation_to_apply = new Mul(MulType.Mod);
          return ctx;
        }
      }
    
      public class RelOpVisitor implements jlc.lib.javalette.Absyn.RelOp.Visitor<Ctx, Ctx> {
        public Ctx visit(jlc.lib.javalette.Absyn.LTH p, Ctx ctx) {
          ctx.last_operation_to_apply = new Rel(RelType.LTH);
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.LE p, Ctx ctx) {
          ctx.last_operation_to_apply = new Rel(RelType.LE);
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.GTH p, Ctx ctx) {
          ctx.last_operation_to_apply = new Rel(RelType.GTH);
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.GE p, Ctx ctx) {
          ctx.last_operation_to_apply = new Rel(RelType.GE);
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.EQU p, Ctx ctx) {
          ctx.last_operation_to_apply = new Rel(RelType.EQU);
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.NE p, Ctx ctx) {
          ctx.last_operation_to_apply = new Rel(RelType.NE);
          return ctx;
        }
      }
}
