package javalette;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.management.RuntimeErrorException;

public class TypeCheckerVisit {
    public class ProgVisitor implements javalette.Absyn.Prog.Visitor<Ctx, Ctx> {
        public Ctx visit(javalette.Absyn.Program p, Ctx ctx) {
          ctx = this.ctx;
          
          for (javalette.Absyn.TopDef x: p.listtopdef_) {
            ctx = x.accept(new TopDefVisitor(), ctx);
          }
    
          return ctx;
        }

        public Ctx ctx;

        public void SetCtx(Ctx ctx) {
            this.ctx = ctx;
            ctx.ctx_variables.clear();
        }
      }
    
      public class TopDefVisitor implements javalette.Absyn.TopDef.Visitor<Ctx, Ctx> {
        public Ctx visit(javalette.Absyn.FnDef p, Ctx ctx) {

          ctx = p.type_.accept(new TypeVisitor(), ctx);
          ctx.ctx_variables.remove("last");

          for (javalette.Absyn.Arg x: p.listarg_) {
            ctx = x.accept(new ArgVisitor(), ctx);
            ctx.ctx_variables.remove("last");
          }

          ctx = p.blk_.accept(new BlkVisitor(), ctx);
          return ctx;
        }
      }
    
      public class ArgVisitor implements javalette.Absyn.Arg.Visitor<Ctx, Ctx> {
        public Ctx visit(javalette.Absyn.Argument p, Ctx ctx) {
          ctx = p.type_.accept(new TypeVisitor(), ctx);
          Variable last = ctx.ctx_variables.get("last");
          last.SetVariableName(p.ident_);
          return ctx;
        }
      }
    
      public class BlkVisitor implements javalette.Absyn.Blk.Visitor<Ctx, Ctx> {
        public Ctx visit(javalette.Absyn.Block p, Ctx ctx) {
          
          Ctx tmp_ctx = ctx.GetNewChildCtx();
          for (javalette.Absyn.Stmt x: p.liststmt_) {
            tmp_ctx = x.accept(new StmtVisitor(), tmp_ctx);
          }
          return ctx;
        }
      }
    
      public class StmtVisitor implements javalette.Absyn.Stmt.Visitor<Ctx, Ctx> {
        public Ctx visit(javalette.Absyn.Empty p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(javalette.Absyn.BStmt p, Ctx ctx) {
          
          ctx = p.blk_.accept(new BlkVisitor(), ctx);
          return ctx;
        }
        public Ctx visit(javalette.Absyn.Decl p, Ctx ctx) {
          
          p.type_.accept(new TypeVisitor(), ctx);
          for (javalette.Absyn.Item x: p.listitem_) {
            ctx = x.accept(new ItemVisitor(), ctx);
            Variable var = ctx.ctx_variables.get("last");

            // Add the new variable with the name and check its existance and also add new vairable with type for further items
            if (ctx.ctx_variables.containsKey(var.GetVariableName())) {
                throw new RuntimeException("Duplicaiton variable name");
            }

            ctx.ctx_variables.put(var.GetVariableName(), var);
            ctx.ctx_variables.remove("last");
            ctx.ctx_variables.put("last", var.GetNewVariableSameType());
          }

          return ctx;
        }
        public Ctx visit(javalette.Absyn.Ass p, Ctx ctx) {
          
          ctx = p.expr_.accept(new ExprVisitor(), ctx);
          return ctx;
        }
        public Ctx visit(javalette.Absyn.Incr p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(javalette.Absyn.Decr p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(javalette.Absyn.Ret p, Ctx ctx) {
          // Clear the varirables tmp
          ctx.ctx_variables.remove("last");
          
          ctx = p.expr_.accept(new ExprVisitor(), ctx);
          return ctx;
        }
        public Ctx visit(javalette.Absyn.VRet p, Ctx ctx) {
          // Clear the varirables tmp
          ctx.ctx_variables.remove("last");
          return ctx;
        }
        public Ctx visit(javalette.Absyn.Cond p, Ctx ctx) {
          
        ctx = p.expr_.accept(new ExprVisitor(), ctx);
        ctx = p.stmt_.accept(new StmtVisitor(), ctx);
          return ctx;
        }
        public Ctx visit(javalette.Absyn.CondElse p, Ctx ctx) {
          
          // Clear the varirables tmp
          ctx.ctx_variables.remove("last");
          ctx = p.expr_.accept(new ExprVisitor(), ctx);


          ctx = p.stmt_1.accept(new StmtVisitor(), ctx);
          ctx = p.stmt_2.accept(new StmtVisitor(), ctx);
          return ctx;
        }
        public Ctx visit(javalette.Absyn.While p, Ctx ctx) {
          
          // Clear the varirables tmp
          ctx.ctx_variables.remove("last");

          ctx = p.expr_.accept(new ExprVisitor(), ctx);
          ctx = p.stmt_.accept(new StmtVisitor(), ctx);
          return ctx;
        }
        public Ctx visit(javalette.Absyn.SExp p, Ctx ctx) {

          // Clear the varirables tmp
          ctx.ctx_variables.remove("last");
          ctx = p.expr_.accept(new ExprVisitor(), ctx);
          return ctx;
        }
      }
    
      public class ItemVisitor implements javalette.Absyn.Item.Visitor<Ctx, Ctx> {
        public Ctx visit(javalette.Absyn.NoInit p, Ctx ctx) {
            Variable var = ctx.ctx_variables.get("last");
            var.SetVariableName(p.ident_);
          return ctx;
        }

        public Ctx visit(javalette.Absyn.Init p, Ctx ctx) {
          
          ctx = p.expr_.accept(new ExprVisitor(), ctx);
          return ctx;
        }
      }
    
      public class TypeVisitor implements javalette.Absyn.Type.Visitor<Ctx, Ctx> {
        public Ctx visit(javalette.Absyn.Int p, Ctx ctx) {
          ctx.ctx_variables.put("last", new IntVariable("last"));
          return ctx;
        }
        public Ctx visit(javalette.Absyn.Doub p, Ctx ctx) {
          
          ctx.ctx_variables.put("last", new DoubleVariable("last"));
          return ctx;
        }
        public Ctx visit(javalette.Absyn.Bool p, Ctx ctx) {
          
          ctx.ctx_variables.put("last", new BooleanVariable("last"));
          return ctx;
        }
        public Ctx visit(javalette.Absyn.Void p, Ctx ctx) {
          
          ctx.ctx_variables.put("last", new VoidVariable("last"));
          return ctx;
        }
        public Ctx visit(javalette.Absyn.Fun p, Ctx ctx) {
          
          // TODO: I dont have any idea what is this
          ctx = p.type_.accept(new TypeVisitor(), ctx);
          for (javalette.Absyn.Type x: p.listtype_) {
            ctx = x.accept(new TypeVisitor(), ctx);
          }
          return ctx;
        }
      }
    
      public class ExprVisitor implements javalette.Absyn.Expr.Visitor<Ctx, Ctx> {
        public Ctx visit(javalette.Absyn.EVar p, Ctx ctx) {
          Variable var = ctx.ctx_variables.get(p.ident_);
          if (var == null) {
            throw new RuntimeException("variable " + p.ident_ + " has not been declared");
          }
          
          if (ctx.ctx_variables.containsKey("last")) {
            throw new RuntimeException("last element exist!");
          }

          ctx.ctx_variables.put("last", var);
          return ctx;
        }

        public Ctx visit(javalette.Absyn.ELitInt p, Ctx ctx) {
          Random rand = new Random();
          Variable var = new IntVariable("tmp_int_variable" + rand.nextInt(100000));
          System.out.println("wtf1 " + p.integer_);
          if (ctx.ctx_variables.containsKey("last")) {
            throw new RuntimeException("last element exist!");
          }

          ctx.ctx_variables.put("last", var);
          return ctx;
        }

        public Ctx visit(javalette.Absyn.ELitDoub p, Ctx ctx) {
          Random rand = new Random();
          Variable var = new DoubleVariable("tmp_double_variable" + rand.nextInt(100000));
          if (ctx.ctx_variables.containsKey("last")) {
            throw new RuntimeException("last element exist!");
          }

          ctx.ctx_variables.put("last", var);
          return ctx;
        }
        public Ctx visit(javalette.Absyn.ELitTrue p, Ctx ctx) {
            Random rand = new Random();
            Variable var = new BooleanVariable("tmp_bool_variable" + rand.nextInt(100000));
            if (ctx.ctx_variables.containsKey("last")) {
              throw new RuntimeException("last element exist!");
            }
  
            ctx.ctx_variables.put("last", var);
            return ctx;
        }

        public Ctx visit(javalette.Absyn.ELitFalse p, Ctx ctx) {
            Random rand = new Random();
            Variable var = new BooleanVariable("tmp_bool_variable" + rand.nextInt(100000));
            if (ctx.ctx_variables.containsKey("last")) {
              throw new RuntimeException("last element exist!");
            }
  
            ctx.ctx_variables.put("last", var);
            return ctx;
        }

        public Ctx visit(javalette.Absyn.EApp p, Ctx ctx) {
          Function fn = ctx.functions.get(p.ident_);
          if (fn == null) {
            throw new RuntimeException("the function " + p.ident_ + " does not exist!");
          }

          List<Variable> args = new ArrayList<>();

          for (javalette.Absyn.Expr x: p.listexpr_) {
            ctx = x.accept(new ExprVisitor(), ctx);

            // Construct args
            Variable var = ctx.ctx_variables.get("last");
            args.add(var);
            ctx.ctx_variables.remove("last");
          }

          boolean canCall = fn.CanCall(args);
          if (!canCall) {
            throw new RuntimeException("cannot call function " + p.ident_ + " with the arguments!");
          }

          System.out.println("wtf");
          ctx.ctx_variables.put("last", fn.GetReturn());

          return ctx;
        }

        public Ctx visit(javalette.Absyn.EString p, Ctx ctx) {
          
          return ctx;
        }

        public Ctx visit(javalette.Absyn.Neg p, Ctx ctx) {
          ctx = p.expr_.accept(new ExprVisitor(), ctx);
          Variable var = ctx.ctx_variables.get("last");
          Operation op = new Neg();

          var = op.Execute(var, new VoidVariable(""));
          ctx.ctx_variables.remove("last");
          ctx.ctx_variables.put("last", var);

          return ctx;
        }

        public Ctx visit(javalette.Absyn.Not p, Ctx ctx) {
          ctx = p.expr_.accept(new ExprVisitor(), ctx);
          Variable var = ctx.ctx_variables.get("last");
          Operation op = new Not();

          var = op.Execute(var, new VoidVariable(""));
          ctx.ctx_variables.remove("last");
          ctx.ctx_variables.put("last", var);
          
          return ctx;
        }

        public Ctx visit(javalette.Absyn.EMul p, Ctx ctx) {
          ctx = p.expr_1.accept(new ExprVisitor(), ctx);
          Variable var1 = ctx.ctx_variables.get("last");
          ctx.ctx_variables.remove("last");

          ctx = p.mulop_.accept(new MulOpVisitor(), ctx);
          Operation op = new Mul();

          ctx = p.expr_2.accept(new ExprVisitor(), ctx);
          Variable var2 = ctx.ctx_variables.get("last");
          ctx.ctx_variables.remove("last");

          Variable result = op.Execute(var1, var2);
          ctx.ctx_variables.put("last", result);

          return ctx;
        }

        public Ctx visit(javalette.Absyn.EAdd p, Ctx ctx) {
            ctx = p.expr_1.accept(new ExprVisitor(), ctx);
            Variable var1 = ctx.ctx_variables.get("last");
            ctx.ctx_variables.remove("last");
  
            ctx = p.addop_.accept(new AddOpVisitor(), ctx);
            Operation op = new Add();
  
            ctx = p.expr_2.accept(new ExprVisitor(), ctx);
            Variable var2 = ctx.ctx_variables.get("last");
            ctx.ctx_variables.remove("last");
  
            Variable result = op.Execute(var1, var2);
            ctx.ctx_variables.put("last", result);
  
            return ctx;
        }

        public Ctx visit(javalette.Absyn.ERel p, Ctx ctx) {
          
            ctx = p.expr_1.accept(new ExprVisitor(), ctx);
            Variable var1 = ctx.ctx_variables.get("last");
            ctx.ctx_variables.remove("last");

            ctx = p.relop_.accept(new RelOpVisitor(), ctx);
            Operation op = new Rel();

            ctx = p.expr_2.accept(new ExprVisitor(), ctx);
            Variable var2 = ctx.ctx_variables.get("last");
            ctx.ctx_variables.remove("last");

            Variable result = op.Execute(var1, var2);
            ctx.ctx_variables.put("last", result);

          return ctx;
        }

        public Ctx visit(javalette.Absyn.EAnd p, Ctx ctx) {
          ctx = p.expr_1.accept(new ExprVisitor(), ctx);
          Variable var1 = ctx.ctx_variables.get("last");
          ctx.ctx_variables.remove("last");

          Operation op = new And();

          ctx = p.expr_2.accept(new ExprVisitor(), ctx);
          Variable var2 = ctx.ctx_variables.get("last");
          ctx.ctx_variables.remove("last");

          Variable result = op.Execute(var1, var2);
          ctx.ctx_variables.put("last", result);

          return ctx;
        }

        public Ctx visit(javalette.Absyn.EOr p, Ctx ctx) {
          ctx = p.expr_1.accept(new ExprVisitor(), ctx);
          Variable var1 = ctx.ctx_variables.get("last");
          ctx.ctx_variables.remove("last");

          Operation op = new Or();

          ctx = p.expr_2.accept(new ExprVisitor(), ctx);
          Variable var2 = ctx.ctx_variables.get("last");
          ctx.ctx_variables.remove("last");

          Variable result = op.Execute(var1, var2);
          ctx.ctx_variables.put("last", result);
          
          return ctx;
        }
      }
    
      public class AddOpVisitor implements javalette.Absyn.AddOp.Visitor<Ctx, Ctx> {
        public Ctx visit(javalette.Absyn.Plus p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(javalette.Absyn.Minus p, Ctx ctx) {
          
          return ctx;
        }
      }
    
      public class MulOpVisitor implements javalette.Absyn.MulOp.Visitor<Ctx, Ctx> {
        public Ctx visit(javalette.Absyn.Times p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(javalette.Absyn.Div p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(javalette.Absyn.Mod p, Ctx ctx) {
          
          return ctx;
        }
      }
    
      public class RelOpVisitor implements javalette.Absyn.RelOp.Visitor<Ctx, Ctx> {
        public Ctx visit(javalette.Absyn.LTH p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(javalette.Absyn.LE p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(javalette.Absyn.GTH p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(javalette.Absyn.GE p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(javalette.Absyn.EQU p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(javalette.Absyn.NE p, Ctx ctx) {
          
          return ctx;
        }
      }
}
