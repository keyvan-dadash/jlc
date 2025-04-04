package javalette;

import java.nio.channels.InterruptibleChannel;
import java.util.Map;

/*** Visitor Design Pattern Skeleton with debug prints. ***/
public class VisitSkel {

  public class ProgVisitor<R,A> implements javalette.Absyn.Prog.Visitor<R,A> {
    public R visit(javalette.Absyn.Program p, A arg) {
      System.out.println("visit: ProgVisitor.visit(javalette.Absyn.Program p, A arg) - " + p);
      for (javalette.Absyn.TopDef x: p.listtopdef_) {
        x.accept(new TopDefVisitor<R,A>(), arg);
      }

      return null;
    }
  }

  public class TopDefVisitor<R,A> implements javalette.Absyn.TopDef.Visitor<R,A> {
    public R visit(javalette.Absyn.FnDef p, A arg) {
      System.out.println("visit: TopDefVisitor.visit(javalette.Absyn.FnDef p, A arg) - " + p);
      p.type_.accept(new TypeVisitor<R,A>(), arg);
      for (javalette.Absyn.Arg x: p.listarg_) {
        x.accept(new ArgVisitor<R,A>(), arg);
      }
      p.blk_.accept(new BlkVisitor<R,A>(), arg);
      return null;
    }
  }

  public class ArgVisitor<R,A> implements javalette.Absyn.Arg.Visitor<R,A> {
    public R visit(javalette.Absyn.Argument p, A arg) {
      System.out.println("visit: ArgVisitor.visit(javalette.Absyn.Argument p, A arg) - " + p);
      p.type_.accept(new TypeVisitor<R,A>(), arg);
      return null;
    }
  }

  public class BlkVisitor<R,A> implements javalette.Absyn.Blk.Visitor<R,A> {
    public R visit(javalette.Absyn.Block p, A arg) {
      System.out.println("visit: BlkVisitor.visit(javalette.Absyn.Block p, A arg) - " + p);
      for (javalette.Absyn.Stmt x: p.liststmt_) {
        x.accept(new StmtVisitor<R,A>(), arg);
      }
      return null;
    }
  }

  public class StmtVisitor<R,A> implements javalette.Absyn.Stmt.Visitor<R,A> {
    public R visit(javalette.Absyn.Empty p, A arg) {
      System.out.println("visit: StmtVisitor.visit(javalette.Absyn.Empty p, A arg) - " + p);
      return null;
    }
    public R visit(javalette.Absyn.BStmt p, A arg) {
      System.out.println("visit: StmtVisitor.visit(javalette.Absyn.BStmt p, A arg) - " + p);
      p.blk_.accept(new BlkVisitor<R,A>(), arg);
      return null;
    }
    public R visit(javalette.Absyn.Decl p, A arg) {
      System.out.println("visit: StmtVisitor.visit(javalette.Absyn.Decl p, A arg) - " + p);
      p.type_.accept(new TypeVisitor<R,A>(), arg);
      for (javalette.Absyn.Item x: p.listitem_) {
        x.accept(new ItemVisitor<R,A>(), arg);
      }
      return null;
    }
    public R visit(javalette.Absyn.Ass p, A arg) {
      System.out.println("visit: StmtVisitor.visit(javalette.Absyn.Ass p, A arg) - " + p);
      p.expr_.accept(new ExprVisitor<R,A>(), arg);
      return null;
    }
    public R visit(javalette.Absyn.Incr p, A arg) {
      System.out.println("visit: StmtVisitor.visit(javalette.Absyn.Incr p, A arg) - " + p);
      return null;
    }
    public R visit(javalette.Absyn.Decr p, A arg) {
      System.out.println("visit: StmtVisitor.visit(javalette.Absyn.Decr p, A arg) - " + p);
      return null;
    }
    public R visit(javalette.Absyn.Ret p, A arg) {
      System.out.println("visit: StmtVisitor.visit(javalette.Absyn.Ret p, A arg) - " + p);
      p.expr_.accept(new ExprVisitor<R,A>(), arg);
      return null;
    }
    public R visit(javalette.Absyn.VRet p, A arg) {
      System.out.println("visit: StmtVisitor.visit(javalette.Absyn.VRet p, A arg) - " + p);
      return null;
    }
    public R visit(javalette.Absyn.Cond p, A arg) {
      System.out.println("visit: StmtVisitor.visit(javalette.Absyn.Cond p, A arg) - " + p);
      p.expr_.accept(new ExprVisitor<R,A>(), arg);
      p.stmt_.accept(new StmtVisitor<R,A>(), arg);
      return null;
    }
    public R visit(javalette.Absyn.CondElse p, A arg) {
      System.out.println("visit: StmtVisitor.visit(javalette.Absyn.CondElse p, A arg) - " + p);
      p.expr_.accept(new ExprVisitor<R,A>(), arg);
      p.stmt_1.accept(new StmtVisitor<R,A>(), arg);
      p.stmt_2.accept(new StmtVisitor<R,A>(), arg);
      return null;
    }
    public R visit(javalette.Absyn.While p, A arg) {
      System.out.println("visit: StmtVisitor.visit(javalette.Absyn.While p, A arg) - " + p);
      p.expr_.accept(new ExprVisitor<R,A>(), arg);
      p.stmt_.accept(new StmtVisitor<R,A>(), arg);
      return null;
    }
    public R visit(javalette.Absyn.SExp p, A arg) {
      System.out.println("visit: StmtVisitor.visit(javalette.Absyn.SExp p, A arg) - " + p);
      p.expr_.accept(new ExprVisitor<R,A>(), arg);
      return null;
    }
  }

  public class ItemVisitor<R,A> implements javalette.Absyn.Item.Visitor<R,A> {
    public R visit(javalette.Absyn.NoInit p, A arg) {
      System.out.println("visit: ItemVisitor.visit(javalette.Absyn.NoInit p, A arg) - " + p);
      return null;
    }
    public R visit(javalette.Absyn.Init p, A arg) {
      System.out.println("visit: ItemVisitor.visit(javalette.Absyn.Init p, A arg) - " + p);
      p.expr_.accept(new ExprVisitor<R,A>(), arg);
      return null;
    }
  }

  public class TypeVisitor<R,A> implements javalette.Absyn.Type.Visitor<R,A> {
    public R visit(javalette.Absyn.Int p, A arg) {
      System.out.println("visit: TypeVisitor.visit(javalette.Absyn.Int p, A arg) - " + p);
      return null;
    }
    public R visit(javalette.Absyn.Doub p, A arg) {
      System.out.println("visit: TypeVisitor.visit(javalette.Absyn.Doub p, A arg) - " + p);
      return null;
    }
    public R visit(javalette.Absyn.Bool p, A arg) {
      System.out.println("visit: TypeVisitor.visit(javalette.Absyn.Bool p, A arg) - " + p);
      return null;
    }
    public R visit(javalette.Absyn.Void p, A arg) {
      System.out.println("visit: TypeVisitor.visit(javalette.Absyn.Void p, A arg) - " + p);
      return null;
    }
    public R visit(javalette.Absyn.Fun p, A arg) {
      System.out.println("visit: TypeVisitor.visit(javalette.Absyn.Fun p, A arg) - " + p);
      p.type_.accept(new TypeVisitor<R,A>(), arg);
      for (javalette.Absyn.Type x: p.listtype_) {
        x.accept(new TypeVisitor<R,A>(), arg);
      }
      return null;
    }
  }

  public class ExprVisitor<R,A> implements javalette.Absyn.Expr.Visitor<R,A> {
    public R visit(javalette.Absyn.EVar p, A arg) {
      System.out.println("visit: ExprVisitor.visit(javalette.Absyn.EVar p, A arg) - " + p);
      return null;
    }
    public R visit(javalette.Absyn.ELitInt p, A arg) {
      System.out.println("visit: ExprVisitor.visit(javalette.Absyn.ELitInt p, A arg) - " + p);
      return null;
    }
    public R visit(javalette.Absyn.ELitDoub p, A arg) {
      System.out.println("visit: ExprVisitor.visit(javalette.Absyn.ELitDoub p, A arg) - " + p);
      return null;
    }
    public R visit(javalette.Absyn.ELitTrue p, A arg) {
      System.out.println("visit: ExprVisitor.visit(javalette.Absyn.ELitTrue p, A arg) - " + p);
      return null;
    }
    public R visit(javalette.Absyn.ELitFalse p, A arg) {
      System.out.println("visit: ExprVisitor.visit(javalette.Absyn.ELitFalse p, A arg) - " + p);
      return null;
    }
    public R visit(javalette.Absyn.EApp p, A arg) {
      System.out.println("visit: ExprVisitor.visit(javalette.Absyn.EApp p, A arg) - " + p);
      for (javalette.Absyn.Expr x: p.listexpr_) {
        x.accept(new ExprVisitor<R,A>(), arg);
      }
      return null;
    }
    public R visit(javalette.Absyn.EString p, A arg) {
      System.out.println("visit: ExprVisitor.visit(javalette.Absyn.EString p, A arg) - " + p);
      return null;
    }
    public R visit(javalette.Absyn.Neg p, A arg) {
      System.out.println("visit: ExprVisitor.visit(javalette.Absyn.Neg p, A arg) - " + p);
      p.expr_.accept(new ExprVisitor<R,A>(), arg);
      return null;
    }
    public R visit(javalette.Absyn.Not p, A arg) {
      System.out.println("visit: ExprVisitor.visit(javalette.Absyn.Not p, A arg) - " + p);
      p.expr_.accept(new ExprVisitor<R,A>(), arg);
      return null;
    }
    public R visit(javalette.Absyn.EMul p, A arg) {
      System.out.println("visit: ExprVisitor.visit(javalette.Absyn.EMul p, A arg) - " + p);
      p.expr_1.accept(new ExprVisitor<R,A>(), arg);
      p.mulop_.accept(new MulOpVisitor<R,A>(), arg);
      p.expr_2.accept(new ExprVisitor<R,A>(), arg);
      return null;
    }
    public R visit(javalette.Absyn.EAdd p, A arg) {
      System.out.println("visit: ExprVisitor.visit(javalette.Absyn.EAdd p, A arg) - " + p);
      p.expr_1.accept(new ExprVisitor<R,A>(), arg);
      p.addop_.accept(new AddOpVisitor<R,A>(), arg);
      p.expr_2.accept(new ExprVisitor<R,A>(), arg);
      return null;
    }
    public R visit(javalette.Absyn.ERel p, A arg) {
      System.out.println("visit: ExprVisitor.visit(javalette.Absyn.ERel p, A arg) - " + p);
      p.expr_1.accept(new ExprVisitor<R,A>(), arg);
      p.relop_.accept(new RelOpVisitor<R,A>(), arg);
      p.expr_2.accept(new ExprVisitor<R,A>(), arg);
      return null;
    }
    public R visit(javalette.Absyn.EAnd p, A arg) {
      System.out.println("visit: ExprVisitor.visit(javalette.Absyn.EAnd p, A arg) - " + p);
      p.expr_1.accept(new ExprVisitor<R,A>(), arg);
      p.expr_2.accept(new ExprVisitor<R,A>(), arg);
      return null;
    }
    public R visit(javalette.Absyn.EOr p, A arg) {
      System.out.println("visit: ExprVisitor.visit(javalette.Absyn.EOr p, A arg) - " + p);
      p.expr_1.accept(new ExprVisitor<R,A>(), arg);
      p.expr_2.accept(new ExprVisitor<R,A>(), arg);
      return null;
    }
  }

  public class AddOpVisitor<R,A> implements javalette.Absyn.AddOp.Visitor<R,A> {
    public R visit(javalette.Absyn.Plus p, A arg) {
      System.out.println("visit: AddOpVisitor.visit(javalette.Absyn.Plus p, A arg) - " + p);
      return null;
    }
    public R visit(javalette.Absyn.Minus p, A arg) {
      System.out.println("visit: AddOpVisitor.visit(javalette.Absyn.Minus p, A arg) - " + p);
      return null;
    }
  }

  public class MulOpVisitor<R,A> implements javalette.Absyn.MulOp.Visitor<R,A> {
    public R visit(javalette.Absyn.Times p, A arg) {
      System.out.println("visit: MulOpVisitor.visit(javalette.Absyn.Times p, A arg) - " + p);
      return null;
    }
    public R visit(javalette.Absyn.Div p, A arg) {
      System.out.println("visit: MulOpVisitor.visit(javalette.Absyn.Div p, A arg) - " + p);
      return null;
    }
    public R visit(javalette.Absyn.Mod p, A arg) {
      System.out.println("visit: MulOpVisitor.visit(javalette.Absyn.Mod p, A arg) - " + p);
      return null;
    }
  }

  public class RelOpVisitor<R,A> implements javalette.Absyn.RelOp.Visitor<R,A> {
    public R visit(javalette.Absyn.LTH p, A arg) {
      System.out.println("visit: RelOpVisitor.visit(javalette.Absyn.LTH p, A arg) - " + p);
      return null;
    }
    public R visit(javalette.Absyn.LE p, A arg) {
      System.out.println("visit: RelOpVisitor.visit(javalette.Absyn.LE p, A arg) - " + p);
      return null;
    }
    public R visit(javalette.Absyn.GTH p, A arg) {
      System.out.println("visit: RelOpVisitor.visit(javalette.Absyn.GTH p, A arg) - " + p);
      return null;
    }
    public R visit(javalette.Absyn.GE p, A arg) {
      System.out.println("visit: RelOpVisitor.visit(javalette.Absyn.GE p, A arg) - " + p);
      return null;
    }
    public R visit(javalette.Absyn.EQU p, A arg) {
      System.out.println("visit: RelOpVisitor.visit(javalette.Absyn.EQU p, A arg) - " + p);
      return null;
    }
    public R visit(javalette.Absyn.NE p, A arg) {
      System.out.println("visit: RelOpVisitor.visit(javalette.Absyn.NE p, A arg) - " + p);
      return null;
    }
  }
}
