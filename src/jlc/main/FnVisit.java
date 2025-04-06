package jlc.main;

import java.util.ArrayList;
import java.util.Arrays;

import jlc.lib.javalette.*;
import jlc.main.Variables.Variable;
import jlc.main.Variables.IntVariable;
import jlc.main.Variables.VoidVariable;
import jlc.main.Variables.BooleanVariable;
import jlc.main.Variables.DoubleVariable;

public class FnVisit {
    public class ProgVisitor implements jlc.lib.javalette.Absyn.Prog.Visitor<Ctx, Ctx> {
        public Ctx visit(jlc.lib.javalette.Absyn.Program p, Ctx ctx) {
          ctx = this.ctx;
          SetDefaultFn(ctx);
          for (jlc.lib.javalette.Absyn.TopDef x: p.listtopdef_) {
            ctx = x.accept(new TopDefVisitor(), ctx);
          }
          return ctx;
        }

        public Ctx ctx;

        public void SetCtx(Ctx ctx) {
            this.ctx = ctx;
        }

        public Ctx GetCtx() {
            return this.ctx;
        }

        public void SetDefaultFn(Ctx ctx) {
            Function printInt = new Function(
                new ArrayList<>(Arrays.asList(new IntVariable("n"))), 
                new VoidVariable("return_printInt"), 
                "printInt");
            ctx.functions.put("printInt", printInt);

            Function printDouble = new Function(
                new ArrayList<>(Arrays.asList(new DoubleVariable("n"))), 
                new VoidVariable("return_printDouble"), 
                "printDouble");
            ctx.functions.put("printDouble", printDouble);
            
            // Fix this
            Function printString = new Function(
                new ArrayList<>(Arrays.asList(new DoubleVariable("n"))), 
                new VoidVariable("return_printString"), 
                "printString");
            ctx.functions.put("printString", printString);
            
            Function readInt = new Function(
                new ArrayList<>(Arrays.asList(new VoidVariable("n"))), 
                new IntVariable("return_readInt"), 
                "readInt");
            ctx.functions.put("readInt", readInt);

            Function readDouble = new Function(
                new ArrayList<>(Arrays.asList(new VoidVariable("n"))), 
                new DoubleVariable("return_readDouble"), 
                "readDouble");
            ctx.functions.put("readDouble", readDouble);
        }
      }
    
      public class TopDefVisitor implements jlc.lib.javalette.Absyn.TopDef.Visitor<Ctx, Ctx> {
        public Ctx visit(jlc.lib.javalette.Absyn.FnDef p, Ctx ctx) {
          

          Function fn = ctx.functions.get(p.ident_);
          if (fn != null) {
            throw new RuntimeException("Dublication function");
          } else {
            ctx.functions.put(p.ident_, new Function());
            fn = ctx.functions.get(p.ident_);
          }

          ctx = p.type_.accept(new TypeVisitor(), ctx);

          // Pop the return variable and set it on function
          Variable last = ctx.ctx_variables.get("last");
          last.SetVariableName(p.ident_ + "_return");
          fn.return_var = last;
          fn.SetFunctionName(p.ident_);
          ctx.ctx_variables.remove("last");

          for (jlc.lib.javalette.Absyn.Arg x: p.listarg_) {
            ctx = x.accept(new ArgVisitor(), ctx);

            // Remove args variable form ctx
            Variable fn_arg = ctx.ctx_variables.get("last");
            ctx.ctx_variables.remove("last");
            fn.func_args.add(fn_arg);
          }

          ctx = p.blk_.accept(new BlkVisitor(), ctx);
          return ctx;
        }
      }
    
      public class ArgVisitor implements jlc.lib.javalette.Absyn.Arg.Visitor<Ctx, Ctx> {
        public Ctx visit(jlc.lib.javalette.Absyn.Argument p, Ctx ctx) {
          
          ctx = p.type_.accept(new TypeVisitor(), ctx);
          Variable last = ctx.ctx_variables.get("last");
          last.SetVariableName(p.ident_);
          return ctx;
        }
      }
    
      public class BlkVisitor implements jlc.lib.javalette.Absyn.Blk.Visitor<Ctx, Ctx> {
        public Ctx visit(jlc.lib.javalette.Absyn.Block p, Ctx ctx) {
          
          for (jlc.lib.javalette.Absyn.Stmt x: p.liststmt_) {
            x.accept(new StmtVisitor(), ctx);
          }
          return ctx;
        }
      }
    
      public class StmtVisitor implements jlc.lib.javalette.Absyn.Stmt.Visitor<Ctx, Ctx> {
        public Ctx visit(jlc.lib.javalette.Absyn.Empty p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.BStmt p, Ctx ctx) {
          
          p.blk_.accept(new BlkVisitor(), ctx);
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.Decl p, Ctx ctx) {
          
          p.type_.accept(new TypeVisitor(), ctx);
          for (jlc.lib.javalette.Absyn.Item x: p.listitem_) {
            x.accept(new ItemVisitor(), ctx);
          }
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.Ass p, Ctx ctx) {
          
          p.expr_.accept(new ExprVisitor(), ctx);
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.Incr p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.Decr p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.Ret p, Ctx ctx) {
          
          p.expr_.accept(new ExprVisitor(), ctx);
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.VRet p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.Cond p, Ctx ctx) {
          
          p.expr_.accept(new ExprVisitor(), ctx);
          p.stmt_.accept(new StmtVisitor(), ctx);
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.CondElse p, Ctx ctx) {
          
          p.expr_.accept(new ExprVisitor(), ctx);
          p.stmt_1.accept(new StmtVisitor(), ctx);
          p.stmt_2.accept(new StmtVisitor(), ctx);
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.While p, Ctx ctx) {
          
          p.expr_.accept(new ExprVisitor(), ctx);
          p.stmt_.accept(new StmtVisitor(), ctx);
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.SExp p, Ctx ctx) {
          
          p.expr_.accept(new ExprVisitor(), ctx);
          return ctx;
        }
      }
    
      public class ItemVisitor implements jlc.lib.javalette.Absyn.Item.Visitor<Ctx, Ctx> {
        public Ctx visit(jlc.lib.javalette.Absyn.NoInit p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.Init p, Ctx ctx) {
          
          p.expr_.accept(new ExprVisitor(), ctx);
          return ctx;
        }
      }
    
      public class TypeVisitor implements jlc.lib.javalette.Absyn.Type.Visitor<Ctx, Ctx> {
        public Ctx visit(jlc.lib.javalette.Absyn.Int p, Ctx ctx) {
          
          ctx.ctx_variables.put("last", new IntVariable("last"));
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.Doub p, Ctx ctx) {
          
          ctx.ctx_variables.put("last", new DoubleVariable("last"));
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.Bool p, Ctx ctx) {
          
          ctx.ctx_variables.put("last", new BooleanVariable("last"));
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.Void p, Ctx ctx) {
          
          ctx.ctx_variables.put("last", new VoidVariable("last"));
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.Fun p, Ctx ctx) {
          
          // TODO: I dont have any idea what is this
          p.type_.accept(new TypeVisitor(), ctx);
          for (jlc.lib.javalette.Absyn.Type x: p.listtype_) {
            x.accept(new TypeVisitor(), ctx);
          }
          return ctx;
        }
      }
    
      public class ExprVisitor implements jlc.lib.javalette.Absyn.Expr.Visitor<Ctx, Ctx> {
        public Ctx visit(jlc.lib.javalette.Absyn.EVar p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.ELitInt p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.ELitDoub p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.ELitTrue p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.ELitFalse p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.EApp p, Ctx ctx) {
          
          for (jlc.lib.javalette.Absyn.Expr x: p.listexpr_) {
            x.accept(new ExprVisitor(), ctx);
          }
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.EString p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.Neg p, Ctx ctx) {
          
          p.expr_.accept(new ExprVisitor(), ctx);
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.Not p, Ctx ctx) {
          
          p.expr_.accept(new ExprVisitor(), ctx);
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.EMul p, Ctx ctx) {
          
          p.expr_1.accept(new ExprVisitor(), ctx);
          p.mulop_.accept(new MulOpVisitor(), ctx);
          p.expr_2.accept(new ExprVisitor(), ctx);
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.EAdd p, Ctx ctx) {
          
          p.expr_1.accept(new ExprVisitor(), ctx);
          p.addop_.accept(new AddOpVisitor(), ctx);
          p.expr_2.accept(new ExprVisitor(), ctx);
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.ERel p, Ctx ctx) {
          
          p.expr_1.accept(new ExprVisitor(), ctx);
          p.relop_.accept(new RelOpVisitor(), ctx);
          p.expr_2.accept(new ExprVisitor(), ctx);
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.EAnd p, Ctx ctx) {
          
          p.expr_1.accept(new ExprVisitor(), ctx);
          p.expr_2.accept(new ExprVisitor(), ctx);
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.EOr p, Ctx ctx) {
          
          p.expr_1.accept(new ExprVisitor(), ctx);
          p.expr_2.accept(new ExprVisitor(), ctx);
          return ctx;
        }
      }
    
      public class AddOpVisitor implements jlc.lib.javalette.Absyn.AddOp.Visitor<Ctx, Ctx> {
        public Ctx visit(jlc.lib.javalette.Absyn.Plus p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.Minus p, Ctx ctx) {
          
          return ctx;
        }
      }
    
      public class MulOpVisitor implements jlc.lib.javalette.Absyn.MulOp.Visitor<Ctx, Ctx> {
        public Ctx visit(jlc.lib.javalette.Absyn.Times p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.Div p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.Mod p, Ctx ctx) {
          
          return ctx;
        }
      }
    
      public class RelOpVisitor implements jlc.lib.javalette.Absyn.RelOp.Visitor<Ctx, Ctx> {
        public Ctx visit(jlc.lib.javalette.Absyn.LTH p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.LE p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.GTH p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.GE p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.EQU p, Ctx ctx) {
          
          return ctx;
        }
        public Ctx visit(jlc.lib.javalette.Absyn.NE p, Ctx ctx) {
          
          return ctx;
        }
      }
}
