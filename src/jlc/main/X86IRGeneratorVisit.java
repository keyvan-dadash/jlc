package jlc.main;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.management.RuntimeErrorException;

import jlc.lib.javalette.Absyn.ArrType;
import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Utils;
import jlc.main.Instructions.x86.IR.AddIR;
import jlc.main.Instructions.x86.IR.AndIR;
import jlc.main.Instructions.x86.IR.AssIR;
import jlc.main.Instructions.x86.IR.CallFnIR;
import jlc.main.Instructions.x86.IR.CmpIR;
import jlc.main.Instructions.x86.IR.FuncDef;
import jlc.main.Instructions.x86.IR.GlobalDoubleIR;
import jlc.main.Instructions.x86.IR.GlobalStringIR;
import jlc.main.Instructions.x86.IR.IR;
import jlc.main.Instructions.x86.IR.JmpEIR;
import jlc.main.Instructions.x86.IR.JmpIR;
import jlc.main.Instructions.x86.IR.JmpNEIR;
import jlc.main.Instructions.x86.IR.LabelIR;
import jlc.main.Instructions.x86.IR.LoadIR;
import jlc.main.Instructions.x86.IR.MulIR;
import jlc.main.Instructions.x86.IR.NegIR;
import jlc.main.Instructions.x86.IR.NotIR;
import jlc.main.Instructions.x86.IR.OrIR;
import jlc.main.Instructions.x86.IR.RelIR;
import jlc.main.Instructions.x86.IR.RetIR;
import jlc.main.Instructions.x86.IR.StoreIR;
import jlc.main.Instructions.x86.IR.TestIR;
import jlc.main.Operations.AddType;
import jlc.main.Operations.MulType;
import jlc.main.Operations.RelType;
import jlc.main.Variables.BooleanVariable;
import jlc.main.Variables.DoubleVariable;
import jlc.main.Variables.IntVariable;
import jlc.main.Variables.StringVariable;
import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableKind;
import jlc.main.Variables.VariableType;
import jlc.main.Variables.VoidVariable;

public class X86IRGeneratorVisit {
    public static final Logger logger = Logger.getLogger(LLVMCodeGeneratorVisit.class.getName());

    public class ProgVisitor implements jlc.lib.javalette.Absyn.Prog.Visitor<X86CodeGenCtx, X86CodeGenCtx> {

        public X86CodeGenCtx ctx;

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.Program p, X86CodeGenCtx ctx) {
          ctx = this.ctx;

          for (jlc.lib.javalette.Absyn.TopDef x: p.listtopdef_) {
            X86CodeGenCtx fnCtx = X86CodeGenCtx.GetSubCtxWithoutVariablesAndParent(ctx);

            // For x86, we have a small problem which is register allocation need to be assigned to unique variable names
            // fnCtx.ResetCounters();
            
            fnCtx = x.accept(new TopDefVisitor(), fnCtx);

            // Copy instruction that has been generated for the function
            ctx.CopyInstructionsFromCtx(fnCtx);

            // Also we should copy the global variable counter
            // For x86, we have a small problem which is register allocation need to be assigned to unique variable names
            // ctx.gloabl_variable_counter = fnCtx.gloabl_variable_counter;
            ctx.CopyCountersFromCtx(fnCtx);
          }

          return ctx;
        }

        // SetX86CodeGenCtx will set a ctx that vistor should use.
        public void SetX86CodeGenCtx(X86CodeGenCtx ctx) {
            logger.fine("Setting up ctx");
            this.ctx = ctx;
        }

        // GetX86CodeGenCtx returns the ctx that this vistor uses.
        public X86CodeGenCtx GetX86CodeGenCtx() {
            return this.ctx;
        }
      }
    
      public class TopDefVisitor implements jlc.lib.javalette.Absyn.TopDef.Visitor<X86CodeGenCtx, X86CodeGenCtx> {

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.FnDef p, X86CodeGenCtx ctx) {
          ctx = p.type_.accept(new TypeVisitor(), ctx);
          Variable retrun_var = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          List<Variable> args = new ArrayList<>();
          for (jlc.lib.javalette.Absyn.Arg x: p.listarg_) {
            ctx = x.accept(new ArgVisitor(), ctx);
            args.add(ctx.GetLastVariable());
            ctx.ClearLastVariable();
          }

          Function fn = new Function();
          fn.fn_name = p.ident_;
          fn.return_var = retrun_var;
          fn.func_args = args;

          ctx.instruction_of_ctx.add(new FuncDef(fn));
          for (int i = 0; i < args.size(); i++) {
            // First, we should rename the argument
            Variable renamedArg = ctx.GetRenamedVariable(args.get(i));
            args.set(i, renamedArg);

            // Also we should put them in ctx vairables
            ctx.AddToCtxVariable(args.get(i));
          }

          ctx.current_func = fn;
          ctx.logic_local_variable = null;

          ctx = p.blk_.accept(new BlkVisitor(), ctx);

          // If this is a void function then the return statement might not be written
          // so we need to check and add return statement instruction to be able to compile.
          if (!ctx.is_ctx_return) {
            ctx.instruction_of_ctx.add(new RetIR(ctx.current_func.fn_name));
            ctx.is_ctx_return = true;
          }

          return ctx;
        }

      }
    
      public class ArgVisitor implements jlc.lib.javalette.Absyn.Arg.Visitor<X86CodeGenCtx, X86CodeGenCtx> {
        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.Argument p, X86CodeGenCtx ctx) {
          ctx = p.type_.accept(new TypeVisitor(), ctx);
          Variable arg = ctx.GetLastVariable();
          ctx.ClearLastVariable();
          arg.SetVariableName(p.ident_);
          ctx.SetLastVariable(arg);
          return ctx;
        }
      }
    
      public class BlkVisitor implements jlc.lib.javalette.Absyn.Blk.Visitor<X86CodeGenCtx, X86CodeGenCtx> {
        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.Block p, X86CodeGenCtx ctx) {
          
          for (jlc.lib.javalette.Absyn.Stmt x: p.liststmt_) {
            x.accept(new StmtVisitor(), ctx);
          }

          return ctx;
        }
      }
    
      public class StmtVisitor implements jlc.lib.javalette.Absyn.Stmt.Visitor<X86CodeGenCtx, X86CodeGenCtx> {

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.ArrAss p, X86CodeGenCtx arg) {
            return arg;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.ArrAssExpr p, X86CodeGenCtx arg) {
            return arg;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.ForEach p, X86CodeGenCtx arg) {
            return arg;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.ArrIncr p, X86CodeGenCtx arg) {
            return arg;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.ArrDecr p, X86CodeGenCtx arg) {
            return arg;
        }
        
        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.Empty p, X86CodeGenCtx ctx) {
          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.BStmt p, X86CodeGenCtx ctx) {
          X86CodeGenCtx blkCtx = X86CodeGenCtx.GetSubCtxWithoutVariables(ctx);
          p.blk_.accept(new BlkVisitor(), blkCtx);
          ctx.CopyInstructionsFromCtx(blkCtx);
          ctx.CopyCountersFromCtx(blkCtx);
          ctx.is_ctx_return = blkCtx.is_ctx_return;
          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.Decl p, X86CodeGenCtx ctx) {
          p.type_.accept(new TypeVisitor(), ctx);

          // This three instruction that does not do anything is for just purpose of not losing
          // outselfs and get confused. The purpose is that the series of next vistors know
          // what would be the type of variable in declaretion.
          Variable typeVariable = ctx.GetLastVariable();
          ctx.ClearLastVariable();
          ctx.SetLastVariable(typeVariable);
          for (jlc.lib.javalette.Absyn.Item x: p.listitem_) {
            x.accept(new ItemVisitor(), ctx);
            // This couple of codes that does not do anything (except put variables inside ctx) is for just purpose of not losing
            // outselfs and get confused.
            typeVariable = ctx.GetLastVariable();

            // Put variables inside ctx
            ctx.AddToCtxVariable(typeVariable);

            ctx.ClearLastVariable();
            ctx.SetLastVariable(typeVariable.GetNewVariableSameType());
          }

          // Cleaning everything
          ctx.ClearLastVariable();

          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.Ass p, X86CodeGenCtx ctx) {
          p.expr_.accept(new ExprVisitor(), ctx);
          Variable resultVariable = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          // We should store resultVariable to the local variable
          Variable localVariable = ctx.GetVariableFromCtx(p.ident_);
          ctx.instruction_of_ctx.add(new AssIR(localVariable, resultVariable));

          // Now, we should invalidate all the previous load instructions that loaded this local variable
          // because now it changed so that we load it again if needed.
          ctx.UnloadVariable(p.ident_);
          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.Incr p, X86CodeGenCtx ctx) {
          Variable one = new IntVariable("1");
          one.SetVariableKind(VariableKind.ConstantVariable);

          // We first need to check if there is any temp register that has this vairable inside it
          ctx = p.expr_.accept(new ExprVisitor(), ctx);
          Variable isTmpExist = ctx.GetLastVariable();
          Variable localVar = ctx.lastLocalVariable;
          if (localVar == null) {
            throw new RuntimeException("couldnt find the local vairable");
          }

          // We have the isTmpExist as the temp register which loaded a local vriable
          Variable newTmp = ctx.GetNewTempVairableWithTheSameTypeOf(isTmpExist);
          ctx.instruction_of_ctx.add(new AddIR(AddType.Plus, newTmp, isTmpExist, one));

          // Now, we need to store it in the local val
          ctx.instruction_of_ctx.add(new StoreIR(localVar, newTmp));

          // Now remove old loaded tmp vairable
          ctx.UnloadVariable(localVar.GetVariableName());

          // Store the new loaded variable
          ctx.AddVariabelAsLoaded(localVar.GetVariableName(), newTmp);

          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.Decr p, X86CodeGenCtx ctx) {
          Variable one = new IntVariable("1");
          one.SetVariableKind(VariableKind.ConstantVariable);

          // We first need to check if there is any temp register that has this vairable inside it
          ctx = p.expr_.accept(new ExprVisitor(), ctx);
          Variable isTmpExist = ctx.GetLastVariable();
          Variable localVar = ctx.lastLocalVariable;
          if (localVar == null) {
            throw new RuntimeException("couldnt find the local vairable");
          }

          // We have the isTmpExist as the temp register which loaded a local vriable
          Variable newTmp = ctx.GetNewTempVairableWithTheSameTypeOf(isTmpExist);
          ctx.instruction_of_ctx.add(new AddIR(AddType.Minus, newTmp, isTmpExist, one));

          // Now, we need to store it in the local val
          ctx.instruction_of_ctx.add(new StoreIR(localVar, newTmp));

          // Now remove old loaded tmp vairable
          ctx.UnloadVariable(localVar.GetVariableName());

          // Store the new loaded variable
          ctx.AddVariabelAsLoaded(localVar.GetVariableName(), newTmp);

          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.Ret p, X86CodeGenCtx ctx) {
          p.expr_.accept(new ExprVisitor(), ctx);
          Variable result = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          ctx.instruction_of_ctx.add(new RetIR(ctx.current_func.fn_name, result));
          ctx.is_ctx_return = true;
          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.VRet p, X86CodeGenCtx ctx) {
          ctx.instruction_of_ctx.add(new RetIR(ctx.current_func.fn_name));
          ctx.is_ctx_return = true;
          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.Cond p, X86CodeGenCtx ctx) {
          // We should create two label. One label is for taking the statement of if
          // the other one is for not taking the statement, and also a jumping for the 
          // statement inside the if.
          List<String> labels = ctx.GetNewLabelWithPrefix("if.end");
          String ifEnd = labels.get(0);

          p.expr_.accept(new ExprVisitor(), ctx);
          Variable conditionResult = ctx.GetLastVariable();

          // We should take care in case result if contant
          if (conditionResult.GetVariableKind() == VariableKind.ConstantVariable) {
            Variable tmp = ctx.GetNewTempVairableWithTheSameTypeOf(conditionResult);
            ctx.instruction_of_ctx.add(new LoadIR(tmp, conditionResult));
            conditionResult = tmp;
          }

          ctx.ClearLastVariable();

          // Lets add the branch ininstruction
          ctx.instruction_of_ctx.add(new TestIR(conditionResult));

          // Now, we add label instruction before adding statements instructions
          ctx.instruction_of_ctx.add(new JmpEIR(ifEnd));

          X86CodeGenCtx ifCtx = X86CodeGenCtx.GetSubCtxWithVariables(ctx);
          p.stmt_.accept(new StmtVisitor(), ifCtx);
          ctx.CopyInstructionsFromCtx(ifCtx);
          ctx.CopyCountersFromCtx(ifCtx);

          // Now, we add label instruction end after adding statements instructions
          ctx.instruction_of_ctx.add(new LabelIR(ifEnd));

          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.CondElse p, X86CodeGenCtx ctx) {
          // We should create three label. One label is for taking the statement of if
          // the other one is for taking the else and the last label is after the if and else.
          List<String> labels = ctx.GetNewLabelWithPrefix("else.statement", "if.end");
          String elseStLabel = labels.get(0);
          String ifEnd = labels.get(1);
          
          p.expr_.accept(new ExprVisitor(), ctx);
          Variable conditionResult = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          // We should take care in case result if contant
          if (conditionResult.GetVariableKind() == VariableKind.ConstantVariable) {
            Variable tmp = ctx.GetNewTempVairableWithTheSameTypeOf(conditionResult);
            ctx.instruction_of_ctx.add(new LoadIR(tmp, conditionResult));
            conditionResult = tmp;
          }

          // Lets add the branch ininstruction
          ctx.instruction_of_ctx.add(new TestIR(conditionResult));

          // Now, we add label instruction before adding statements instructions
          ctx.instruction_of_ctx.add(new JmpEIR(elseStLabel));

          X86CodeGenCtx ifCtx = X86CodeGenCtx.GetSubCtxWithVariables(ctx);
          p.stmt_1.accept(new StmtVisitor(), ifCtx);
          ctx.CopyInstructionsFromCtx(ifCtx);
          ctx.CopyCountersFromCtx(ifCtx);

          if (!ifCtx.is_ctx_return) {
            // Now, we should add jump to the if end inside if the statment
            ctx.instruction_of_ctx.add(new JmpIR(ifEnd));
          }

          // Now, we add else label instruction before adding else statements instructions
          ctx.instruction_of_ctx.add(new LabelIR(elseStLabel));

          X86CodeGenCtx elseCtx = X86CodeGenCtx.GetSubCtxWithVariables(ctx);
          p.stmt_2.accept(new StmtVisitor(), elseCtx);
          ctx.CopyInstructionsFromCtx(elseCtx);
          ctx.CopyCountersFromCtx(elseCtx);

          if (!ifCtx.is_ctx_return || !elseCtx.is_ctx_return) {
            // Now, we add label instruction end after adding statements instructions
            ctx.instruction_of_ctx.add(new LabelIR(ifEnd));
          }

          ctx.is_ctx_return = ifCtx.is_ctx_return && elseCtx.is_ctx_return;

          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.While p, X86CodeGenCtx ctx) {
          // We should create three label. One label is for re-evaluting condition of while
          // and the second one for taking the statment of while and the last one is for 
          // end of the while.
          List<String> labels = ctx.GetNewLabelWithPrefix("while.cond", "while.end");
          String whileCondLabel = labels.get(0);
          String whileEndLabel = labels.get(1);

          // Now, we add label instruction before expression in order to re-evalute the
          // condition in each iteration of loop.
          ctx.instruction_of_ctx.add(new LabelIR(whileCondLabel));
 
          p.expr_.accept(new ExprVisitor(), ctx);
          Variable conditionResult = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          // We should take care in case result if contant
          if (conditionResult.GetVariableKind() == VariableKind.ConstantVariable) {
            Variable tmp = ctx.GetNewTempVairableWithTheSameTypeOf(conditionResult);
            ctx.instruction_of_ctx.add(new LoadIR(tmp, conditionResult));
            conditionResult = tmp;
          }

          // We should add the branching instruction after the expression results finished.
          ctx.instruction_of_ctx.add(new TestIR(conditionResult));

          // We should add the while statement label
          ctx.instruction_of_ctx.add(new JmpEIR(whileEndLabel));

          p.stmt_.accept(new StmtVisitor(), ctx);

          // Now, we should jump back to the condition to re-evaluate
          ctx.instruction_of_ctx.add(new JmpIR(whileCondLabel));


          // Here we add the end label so that if the statement of while did not take
          // we continue execution
          ctx.instruction_of_ctx.add(new LabelIR(whileEndLabel));

          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.SExp p, X86CodeGenCtx ctx) {
          p.expr_.accept(new ExprVisitor(), ctx);
          ctx.ClearLastVariable();
          return ctx;
        }
      }
    
      public class ItemVisitor implements jlc.lib.javalette.Absyn.Item.Visitor<X86CodeGenCtx, X86CodeGenCtx> {
        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.NoInit p, X86CodeGenCtx ctx) {

          // Get the type of this variable from typevistor that has been set
          Variable var = ctx.GetLastVariable();
          ctx.ClearLastVariable();
          var.SetVariableName(p.ident_);

          // Lets renamed the variable
          var = ctx.GetRenamedVariable(var);

          // Now, we need to load the default value into the variable
          if (var.GetVariableType() != VariableType.Double) {
            ctx.instruction_of_ctx.add(new StoreIR(var, Utils.GetDefaultValueOfVariableType(var.GetVariableType())));
          } else {
            Variable gVariable = ctx.GetNewGlobalVairableWithTheSameTypeOf(new DoubleVariable(""));
            gVariable.SetVariableKind(VariableKind.GlobalVariable);

            // Lets add global instruction
            ctx.global_instructions.add(new GlobalDoubleIR(gVariable.GetVariableName(), 0.0));

            ctx.instruction_of_ctx.add(new LoadIR(var, gVariable));
          }

          ctx.current_func.func_local.add(var);

          // Set so that future declaration in the same token get variable of same type
          ctx.SetLastVariable(var);

          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.Init p, X86CodeGenCtx ctx) {
          // Get the type of this variable from typevistor that has been set
          Variable var = ctx.GetLastVariable();
          ctx.ClearLastVariable();
          var.SetVariableName(p.ident_);

          // Lets renamed the variable
          var = ctx.GetRenamedVariable(var);

          // Now lets evaluate its init value
          p.expr_.accept(new ExprVisitor(), ctx);
          Variable var1 = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          // Now, we need to load the result into the variable
          ctx.instruction_of_ctx.add(new StoreIR(var, var1));

          ctx.current_func.func_local.add(var);

          // Set so that future declaration in the same token get variable of same type
          ctx.SetLastVariable(var);
          
          return ctx;
        }
      }
    
      public class TypeVisitor implements jlc.lib.javalette.Absyn.Type.Visitor<X86CodeGenCtx, X86CodeGenCtx> {

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.TypeBase p, X86CodeGenCtx arg) {
            p.basetype_.accept(new BaseTypeVisitor(), arg);
            return arg;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.Fun p, X86CodeGenCtx ctx) {
          // TODO: I dont have any idea what is this
          p.type_.accept(new TypeVisitor(), ctx);
          for (jlc.lib.javalette.Absyn.Type x: p.listtype_) {
            x.accept(new TypeVisitor(), ctx);
          }
          return ctx;
        }

        @Override
        public X86CodeGenCtx visit(ArrType p, X86CodeGenCtx ctx) {
          return ctx;
        }
      }

      public class BaseTypeVisitor implements jlc.lib.javalette.Absyn.BaseType.Visitor<X86CodeGenCtx, X86CodeGenCtx> {

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.Int p, X86CodeGenCtx ctx) {
          ctx.SetLastVariable(new IntVariable("last"));
          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.Doub p, X86CodeGenCtx ctx) {
          ctx.SetLastVariable(new DoubleVariable("last"));
          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.Bool p, X86CodeGenCtx ctx) {
          ctx.SetLastVariable(new BooleanVariable("last"));
          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.Void p, X86CodeGenCtx ctx) {
          ctx.SetLastVariable(new VoidVariable("last"));
          return ctx;
        }
      }
    
      public class ExprVisitor implements jlc.lib.javalette.Absyn.Expr.Visitor<X86CodeGenCtx, X86CodeGenCtx> {

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.ArrIndex p, X86CodeGenCtx arg) {
            return arg;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.ArrLen p, X86CodeGenCtx arg) {
            return arg;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.NewArr p, X86CodeGenCtx arg) {
            return arg;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.EVar p, X86CodeGenCtx ctx) {
          // We first need to check if there is any temp register that has this vairable inside it
          Variable isTmpExist = ctx.GetVariableIfLoaded(p.ident_);
          Variable localVar = ctx.GetVariableFromCtx(p.ident_);
          ctx.lastLocalVariable = localVar;
          if (isTmpExist == null) {
            // We need to load the variable
            isTmpExist = ctx.GetNewTempVairableWithTheSameTypeOf(localVar);
            ctx.instruction_of_ctx.add(new LoadIR(isTmpExist, localVar));

            // System.out.printf("%s %s\n", localVar.GetVariableName(), isTmpExist.GetVariableName());
            // We store it as a vriable that has already loaded a local vriable
            ctx.AddVariabelAsLoaded(localVar.GetVariableName(), isTmpExist);
          }

          // We have the isTmpExist as the temp register which loaded a local vriable
          ctx.SetLastVariable(isTmpExist);
          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.ELitInt p, X86CodeGenCtx ctx) {
          Variable var = new IntVariable(p.integer_.toString());
          var.SetVariableKind(VariableKind.ConstantVariable);
          ctx.SetLastVariable(var);
          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.ELitDoub p, X86CodeGenCtx ctx) {
          Variable gVariable = ctx.GetNewGlobalVairableWithTheSameTypeOf(new DoubleVariable(""));
          gVariable.SetVariableKind(VariableKind.GlobalVariable);

          // Lets add global instruction
          ctx.global_instructions.add(new GlobalDoubleIR(gVariable.GetVariableName(), p.double_));

          Variable var = ctx.GetNewTempVairableWithTheSameTypeOf(gVariable);

          ctx.instruction_of_ctx.add(new LoadIR(var, gVariable));

          ctx.SetLastVariable(var);
          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.ELitTrue p, X86CodeGenCtx ctx) {
          Variable var = new BooleanVariable("1");
          var.SetVariableKind(VariableKind.ConstantVariable);
          ctx.SetLastVariable(var);
          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.ELitFalse p, X86CodeGenCtx ctx) {
          Variable var = new BooleanVariable("0");
          var.SetVariableKind(VariableKind.ConstantVariable);
          ctx.SetLastVariable(var);
          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.EApp p, X86CodeGenCtx ctx) {
          
          List<Variable> args = new ArrayList<>();
          for (jlc.lib.javalette.Absyn.Expr x: p.listexpr_) {
            x.accept(new ExprVisitor(), ctx);
            Variable arg = ctx.GetLastVariable();
            ctx.ClearLastVariable();
            args.add(arg);
          }

          Function fn = ctx.functions.get(p.ident_);
          // Lets create a tmp variable same type of the return value of function
          Variable tmp = ctx.GetNewTempVairableWithTheSameTypeOf(fn.return_var);

          ctx.instruction_of_ctx.add(new CallFnIR(fn, args, tmp));

          // Set the result for rest of the tree
          ctx.SetLastVariable(tmp);
          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.EString p, X86CodeGenCtx ctx) {
          // First, we should get a global variable
          Variable gVariable = ctx.GetNewGlobalVairableWithTheSameTypeOf(new StringVariable(""));
          gVariable.SetVariableKind(VariableKind.GlobalVariable);

          // Lets add global instruction
          ctx.global_instructions.add(new GlobalStringIR(gVariable.GetVariableName(), p.string_));

          ctx.global_strings.put(gVariable.GetVariableName(), p.string_);
          ctx.SetLastVariable(gVariable);
          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.Neg p, X86CodeGenCtx ctx) {
          p.expr_.accept(new ExprVisitor(), ctx);
          Variable result = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          // Lets get new temp variable
          Variable tempVariable = ctx.GetNewTempVairableWithTheSameTypeOf(result);
          ctx.instruction_of_ctx.add(new NegIR(tempVariable, result));

          // Set the result now
          ctx.SetLastVariable(tempVariable);
          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.Not p, X86CodeGenCtx ctx) {
          p.expr_.accept(new ExprVisitor(), ctx);
          Variable result = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          // We should take care in case result if contant
          if (result.GetVariableKind() == VariableKind.ConstantVariable) {
            Variable tmp = ctx.GetNewTempVairableWithTheSameTypeOf(result);
            ctx.instruction_of_ctx.add(new LoadIR(tmp, result));
            result = tmp;
          }

          // Lets get new temp variable
          Variable tempVariable = ctx.GetNewTempVairableWithTheSameTypeOf(result);
          ctx.instruction_of_ctx.add(new NotIR(tempVariable, result));

          // Set the result now
          ctx.SetLastVariable(tempVariable);
          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.EMul p, X86CodeGenCtx ctx) {
          p.expr_1.accept(new ExprVisitor(), ctx);
          Variable var1 = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          // We should take care in case result if contant
          if (var1.GetVariableKind() == VariableKind.ConstantVariable) {
            Variable tmp = ctx.GetNewTempVairableWithTheSameTypeOf(var1);
            ctx.instruction_of_ctx.add(new LoadIR(tmp, var1));
            var1 = tmp;
          }

          p.mulop_.accept(new MulOpVisitor(), ctx);
          IR ir = ctx.GetLastInstruction();
          ctx.ClearLastInstruction();
          assert(ir instanceof MulIR);
          MulIR mulIR = (MulIR) ir;

          p.expr_2.accept(new ExprVisitor(), ctx);
          Variable var2 = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          // Now lets generate the rel operation
          Variable tmp = ctx.GetNewTempVairableWithTheSameTypeOf(var2);
          mulIR.setOperands(tmp, var1, var2);
          ctx.instruction_of_ctx.add(mulIR);

          // Set the result for rest of the tree
          ctx.SetLastVariable(tmp);

          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.EAdd p, X86CodeGenCtx ctx) {
          p.expr_1.accept(new ExprVisitor(), ctx);
          Variable var1 = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          // We should take care in case result if contant
          if (var1.GetVariableKind() == VariableKind.ConstantVariable) {
            Variable tmp = ctx.GetNewTempVairableWithTheSameTypeOf(var1);
            ctx.instruction_of_ctx.add(new LoadIR(tmp, var1));
            var1 = tmp;
          }

          p.addop_.accept(new AddOpVisitor(), ctx);
          IR ir = ctx.GetLastInstruction();
          ctx.ClearLastInstruction();
          assert(ir instanceof AddIR);
          AddIR addIR = (AddIR) ir;

          p.expr_2.accept(new ExprVisitor(), ctx);
          Variable var2 = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          // Now lets generate the rel operation
          Variable tmp = ctx.GetNewTempVairableWithTheSameTypeOf(var2);
          addIR.setOperands(tmp, var1, var2);
          ctx.instruction_of_ctx.add(addIR);

          // Set the result for rest of the tree
          ctx.SetLastVariable(tmp);

          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.ERel p, X86CodeGenCtx ctx) {
          p.expr_1.accept(new ExprVisitor(), ctx);
          Variable var1 = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          // We should take care in case result if contant
          if (var1.GetVariableKind() == VariableKind.ConstantVariable) {
            Variable tmp = ctx.GetNewTempVairableWithTheSameTypeOf(var1);
            ctx.instruction_of_ctx.add(new LoadIR(tmp, var1));
            var1 = tmp;
          }

          p.relop_.accept(new RelOpVisitor(), ctx);
          IR ir = ctx.GetLastInstruction();
          ctx.ClearLastInstruction();
          assert(ir instanceof RelIR);
          RelIR relIR = (RelIR) ir;

          p.expr_2.accept(new ExprVisitor(), ctx);
          Variable var2 = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          // Now lets generate the rel operation
          // result of rel operation is always boolean
          Variable tmp = ctx.GetNewTempVairableWithTheSameTypeOf(new BooleanVariable(""));
          relIR.setOperands(tmp, var1, var2);
          ctx.instruction_of_ctx.add(relIR);

          // Set the result for rest of the tree
          ctx.SetLastVariable(tmp);

          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.EAnd p, X86CodeGenCtx ctx) {
          if (ctx.logic_local_variable == null) {
            ctx.logic_local_variable = new BooleanVariable("logical_result");
            ctx.current_func.func_local.add(ctx.logic_local_variable);
          }

          // Before evaluating first part of and we should jump to a stupid label so that phi 
          // instruction can decide later based on it
          List<String> labels = ctx.GetNewLabelWithPrefix("and.end");
          String andEnd = labels.get(0);

          // Now we evaluate the first part of and
          p.expr_1.accept(new ExprVisitor(), ctx);
          Variable var1 = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          // We should take care in case result if contant
          if (var1.GetVariableKind() == VariableKind.ConstantVariable) {
            Variable tmp = ctx.GetNewTempVairableWithTheSameTypeOf(var1);
            ctx.instruction_of_ctx.add(new LoadIR(tmp, var1));
            var1 = tmp;
          }

          // Store the result in the logical_result
          ctx.instruction_of_ctx.add(new StoreIR(ctx.logic_local_variable, var1));

          // Now lets see if the and is true so we can go to the second part
          ctx.instruction_of_ctx.add(new TestIR(var1));

          // Now lets see if the and is true so we can go to the second part
          ctx.instruction_of_ctx.add(new JmpEIR(andEnd));

          p.expr_2.accept(new ExprVisitor(), ctx);
          Variable var2 = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          // Lets write the and instruction of both var1 and var2
          Variable tmp = ctx.GetNewTempVairableWithTheSameTypeOf(var2);
          ctx.instruction_of_ctx.add(new AndIR(tmp, var1, var2));

          // Store result in the logical result 
          ctx.instruction_of_ctx.add(new StoreIR(ctx.logic_local_variable, tmp));

          // Lets add the end label
          ctx.instruction_of_ctx.add(new LabelIR(andEnd));

          // Now we have to load it for the future
          Variable tmp1 = ctx.GetNewTempVairableWithTheSameTypeOf(ctx.logic_local_variable);
          ctx.instruction_of_ctx.add(new LoadIR(tmp1, ctx.logic_local_variable));

          ctx.SetLastVariable(tmp1);
          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.EOr p, X86CodeGenCtx ctx) {
          if (ctx.logic_local_variable == null) {
            ctx.logic_local_variable = new BooleanVariable("logical_result");
            ctx.current_func.func_local.add(ctx.logic_local_variable);
          }

          // Before evaluating first part of or we should jump to a stupid label so that phi 
          // instruction can decide later based on it
          List<String> labels = ctx.GetNewLabelWithPrefix("or.end");
          String orEnd = labels.get(0);

          // Now we evaluate the first part of or
          p.expr_1.accept(new ExprVisitor(), ctx);
          Variable var1 = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          // We should take care in case result if contant
          if (var1.GetVariableKind() == VariableKind.ConstantVariable) {
            Variable tmp = ctx.GetNewTempVairableWithTheSameTypeOf(var1);
            ctx.instruction_of_ctx.add(new LoadIR(tmp, var1));
            var1 = tmp;
          }

          // Store the result in the logical_result
          ctx.instruction_of_ctx.add(new StoreIR(ctx.logic_local_variable, var1));

          // Now lets see if the or is false so we can go to the second part
          ctx.instruction_of_ctx.add(new TestIR(var1));

          // Now lets see if the or is true so we can go to the second part
          ctx.instruction_of_ctx.add(new JmpNEIR(orEnd));

          p.expr_2.accept(new ExprVisitor(), ctx);
          Variable var2 = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          // Lets write the or instruction of both var1 and var2
          Variable tmp = ctx.GetNewTempVairableWithTheSameTypeOf(var2);
          ctx.instruction_of_ctx.add(new OrIR(tmp, var1, var2));

          // Store result in the logical result 
          ctx.instruction_of_ctx.add(new StoreIR(ctx.logic_local_variable, tmp));

          // Lets add the end label
          ctx.instruction_of_ctx.add(new LabelIR(orEnd));

          // Now we have to load it for the future
          Variable tmp1 = ctx.GetNewTempVairableWithTheSameTypeOf(ctx.logic_local_variable);
          ctx.instruction_of_ctx.add(new LoadIR(tmp1, ctx.logic_local_variable));

          ctx.SetLastVariable(tmp1);
          return ctx;
        }
      }
    
      public class AddOpVisitor implements jlc.lib.javalette.Absyn.AddOp.Visitor<X86CodeGenCtx, X86CodeGenCtx> {
        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.Plus p, X86CodeGenCtx ctx) {
          ctx.SetLastInstruction(new AddIR(AddType.Plus));
          return ctx;
        }
        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.Minus p, X86CodeGenCtx ctx) {
          ctx.SetLastInstruction(new AddIR(AddType.Minus));
          return ctx;
        }
      }
    
      public class MulOpVisitor implements jlc.lib.javalette.Absyn.MulOp.Visitor<X86CodeGenCtx, X86CodeGenCtx> {
        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.Times p, X86CodeGenCtx ctx) {
          ctx.SetLastInstruction(new MulIR(MulType.Times));
          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.Div p, X86CodeGenCtx ctx) {
          ctx.SetLastInstruction(new MulIR(MulType.Div));
          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.Mod p, X86CodeGenCtx ctx) {
          ctx.SetLastInstruction(new MulIR(MulType.Mod));
          return ctx;
        }
      }
    
      public class RelOpVisitor implements jlc.lib.javalette.Absyn.RelOp.Visitor<X86CodeGenCtx, X86CodeGenCtx> {
        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.LTH p, X86CodeGenCtx ctx) {
          ctx.SetLastInstruction(new RelIR(RelType.LTH));
          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.LE p, X86CodeGenCtx ctx) {
          ctx.SetLastInstruction(new RelIR(RelType.LE));
          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.GTH p, X86CodeGenCtx ctx) {
          ctx.SetLastInstruction(new RelIR(RelType.GTH));
          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.GE p, X86CodeGenCtx ctx) {
          ctx.SetLastInstruction(new RelIR(RelType.GE));
          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.EQU p, X86CodeGenCtx ctx) {
          ctx.SetLastInstruction(new RelIR(RelType.EQU));
          return ctx;
        }

        public X86CodeGenCtx visit(jlc.lib.javalette.Absyn.NE p, X86CodeGenCtx ctx) {
          ctx.SetLastInstruction(new RelIR(RelType.NE));
          return ctx;
        }
      }
}
