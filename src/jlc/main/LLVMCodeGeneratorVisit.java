package jlc.main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.LLVM.LLVMAddInstruction;
import jlc.main.Instructions.LLVM.LLVMAllocaInstruction;
import jlc.main.Instructions.LLVM.LLVMAndInstruction;
import jlc.main.Instructions.LLVM.LLVMFuncCallIntruction;
import jlc.main.Instructions.LLVM.LLVMFuncDefenition;
import jlc.main.Instructions.LLVM.LLVMFuncDefenitionEnd;
import jlc.main.Instructions.LLVM.LLVMJmpInstruction;
import jlc.main.Instructions.LLVM.LLVMLabelInstruction;
import jlc.main.Instructions.LLVM.LLVMLoadInstruction;
import jlc.main.Instructions.LLVM.LLVMMulInstruction;
import jlc.main.Instructions.LLVM.LLVMOrInstruction;
import jlc.main.Instructions.LLVM.LLVMRelInstruction;
import jlc.main.Instructions.LLVM.LLVMReturnInstruction;
import jlc.main.Instructions.LLVM.LLVMStoreInstruction;
import jlc.main.Instructions.LLVM.LLVMUnreachableInstruction;
import jlc.main.Operations.AddType;
import jlc.main.Operations.MulType;
import jlc.main.Operations.RelType;
import jlc.main.Variables.BooleanVariable;
import jlc.main.Variables.DoubleVariable;
import jlc.main.Variables.IntVariable;
import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableKind;
import jlc.main.Variables.VoidVariable;

public class LLVMCodeGeneratorVisit {
    public static final Logger logger = Logger.getLogger(LLVMCodeGeneratorVisit.class.getName());

    public class ProgVisitor implements jlc.lib.javalette.Absyn.Prog.Visitor<LLVMCodeGenCtx, LLVMCodeGenCtx> {

        public LLVMCodeGenCtx ctx;

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Program p, LLVMCodeGenCtx ctx) {
          ctx = this.ctx;

          for (jlc.lib.javalette.Absyn.TopDef x: p.listtopdef_) {
            LLVMCodeGenCtx fnCtx = LLVMCodeGenCtx.GetSubCtxWithoutVariables(ctx);
            fnCtx.ResetCounters();
            fnCtx = x.accept(new TopDefVisitor(), fnCtx);

            // Copy instruction that has been generated for the function
            ctx.CopyInstructionsFromCtx(fnCtx);
          }

          return ctx;
        }

        // SetLLVMCodeGenCtx will set a ctx that vistor should use.
        public void SetLLVMCodeGenCtx(LLVMCodeGenCtx ctx) {
            logger.fine("Setting up ctx");
            this.ctx = ctx;
        }

        // GetLLVMCodeGenCtx returns the ctx that this vistor uses.
        public LLVMCodeGenCtx GetLLVMCodeGenCtx() {
            return this.ctx;
        }
      }
    
      public class TopDefVisitor implements jlc.lib.javalette.Absyn.TopDef.Visitor<LLVMCodeGenCtx, LLVMCodeGenCtx> {

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.FnDef p, LLVMCodeGenCtx ctx) {
          ctx = p.type_.accept(new TypeVisitor(), ctx);
          Variable retrun_var = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          List<Variable> args = new ArrayList<>();
          List<Variable> args_to_allocate = new ArrayList<>();
          for (jlc.lib.javalette.Absyn.Arg x: p.listarg_) {
            ctx = x.accept(new ArgVisitor(), ctx);
            Variable arg = ctx.GetLastVariable();
            Variable newArg = arg.GetNewVariableSameType();
            newArg.SetVariableName("__p__" + arg.GetVariableName());

            args.add(newArg);
            args_to_allocate.add(arg);
            ctx.ClearLastVariable();
          }

          Function fn = new Function();
          fn.fn_name = p.ident_;
          fn.return_var = retrun_var;
          fn.func_args = args;

          LLVMFuncDefenition llvmFuncDefenition = new LLVMFuncDefenition(fn);
          llvmFuncDefenition.AddNumOfSpaceForPrefix(0);
          ctx.instruction_of_ctx.add(llvmFuncDefenition);

          for (int i = 0; i < args_to_allocate.size(); i++) {
            // First we should allocate the arg
            LLVMAllocaInstruction llvmAllocaInstruction = new LLVMAllocaInstruction(
                args_to_allocate.get(i),
                args_to_allocate.get(i).GetVariableType());
            llvmAllocaInstruction.AddNumOfSpaceForPrefix(4);
            ctx.instruction_of_ctx.add(llvmAllocaInstruction);

            // Now we add store instruction
            LLVMStoreInstruction llvmStoreInstruction = new LLVMStoreInstruction(args.get(i), args_to_allocate.get(i));
            llvmStoreInstruction.AddNumOfSpaceForPrefix(4);
            ctx.instruction_of_ctx.add(llvmStoreInstruction);

            // we can put the arg in side the loaded variable since it is a register
            ctx.loaded_variables.put(args_to_allocate.get(i).GetVariableName(), args.get(i));

            // Also we should put them in ctx vairables
            ctx.ctx_variables.put(args_to_allocate.get(i).GetVariableName(), args_to_allocate.get(i));
          }

          ctx = p.blk_.accept(new BlkVisitor(), ctx);

          // Add an unreach before function end so that we dont have to handle
          // if and elses that return and our branching will get messed up.
          LLVMUnreachableInstruction llvmUnreachableInstruction = new LLVMUnreachableInstruction();
          llvmUnreachableInstruction.AddNumOfSpaceForPrefix(4);
          ctx.instruction_of_ctx.add(llvmUnreachableInstruction);

          LLVMFuncDefenitionEnd llvmFuncDefenitionEnd = new LLVMFuncDefenitionEnd();
          llvmFuncDefenitionEnd.AddNumOfSpaceForPrefix(0);
          ctx.instruction_of_ctx.add(llvmFuncDefenitionEnd);

          return ctx;
        }

      }
    
      public class ArgVisitor implements jlc.lib.javalette.Absyn.Arg.Visitor<LLVMCodeGenCtx, LLVMCodeGenCtx> {
        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Argument p, LLVMCodeGenCtx ctx) {
          ctx = p.type_.accept(new TypeVisitor(), ctx);
          Variable arg = ctx.GetLastVariable();
          ctx.ClearLastVariable();
          arg.SetVariableName(p.ident_);
          ctx.SetLastVariable(arg);
          return ctx;
        }
      }
    
      public class BlkVisitor implements jlc.lib.javalette.Absyn.Blk.Visitor<LLVMCodeGenCtx, LLVMCodeGenCtx> {
        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Block p, LLVMCodeGenCtx ctx) {
          
          for (jlc.lib.javalette.Absyn.Stmt x: p.liststmt_) {
            x.accept(new StmtVisitor(), ctx);
          }

          return ctx;
        }
      }
    
      public class StmtVisitor implements jlc.lib.javalette.Absyn.Stmt.Visitor<LLVMCodeGenCtx, LLVMCodeGenCtx> {
        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Empty p, LLVMCodeGenCtx ctx) {
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.BStmt p, LLVMCodeGenCtx ctx) {
          // TODO: new context?
          p.blk_.accept(new BlkVisitor(), ctx);
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Decl p, LLVMCodeGenCtx ctx) {
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
            ctx.ctx_variables.put(typeVariable.GetVariableName(), typeVariable);

            ctx.ClearLastVariable();
            ctx.SetLastVariable(typeVariable.GetNewVariableSameType());
          }

          // Cleaning everything
          ctx.ClearLastVariable();

          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Ass p, LLVMCodeGenCtx ctx) {
          p.expr_.accept(new ExprVisitor(), ctx);
          Variable resultVariable = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          // We should store resultVariable to the local variable
          Variable localVariable = ctx.ctx_variables.get(p.ident_);
          LLVMStoreInstruction llvmStoreInstruction = new LLVMStoreInstruction(resultVariable, localVariable);
          llvmStoreInstruction.AddNumOfSpaceForPrefix(4);
          ctx.instruction_of_ctx.add(llvmStoreInstruction);

          // Now, we should invalidate all the previous load instructions that loaded this local variable
          // because now it changed so that we load it again if needed.
          ctx.loaded_variables.remove(p.ident_);
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Incr p, LLVMCodeGenCtx ctx) {
          Variable one = new IntVariable("1");
          one.SetVariableKind(VariableKind.ConstantVariable);

          // We first need to check if there is any temp register that has this vairable inside it
          Variable isTmpExist = ctx.loaded_variables.get(p.ident_);
          Variable localVar = ctx.ctx_variables.get(p.ident_);;
          if (isTmpExist == null) {
            // We need to load the variable
            isTmpExist = ctx.GetNewTempVairableWithTheSameTypeOf(localVar);
            LLVMLoadInstruction llvmLoadInstruction = new LLVMLoadInstruction(localVar, isTmpExist);
            llvmLoadInstruction.AddNumOfSpaceForPrefix(4);
            ctx.instruction_of_ctx.add(llvmLoadInstruction);

            // We store it as a vriable that has already loaded a local vriable
            ctx.loaded_variables.put(localVar.GetVariableName(), isTmpExist);
          }

          // We have the isTmpExist as the temp register which loaded a local vriable
          Variable newTmp = ctx.GetNewTempVairableWithTheSameTypeOf(isTmpExist);
          LLVMAddInstruction llvmAddInstruction = new LLVMAddInstruction(AddType.Plus, isTmpExist, one, newTmp);
          llvmAddInstruction.AddNumOfSpaceForPrefix(4);
          ctx.instruction_of_ctx.add(llvmAddInstruction);

          // Now, we need to store it in the local val
          LLVMStoreInstruction llvmStoreInstruction = new LLVMStoreInstruction(newTmp, localVar);
          llvmStoreInstruction.AddNumOfSpaceForPrefix(4);
          ctx.instruction_of_ctx.add(llvmStoreInstruction);

          // Now remove old loaded tmp vairable
          ctx.loaded_variables.remove(localVar.GetVariableName());

          // Store the new loaded variable
          ctx.loaded_variables.put(localVar.GetVariableName(), newTmp);

          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Decr p, LLVMCodeGenCtx ctx) {
          Variable one = new IntVariable("1");
          one.SetVariableKind(VariableKind.ConstantVariable);

          // We first need to check if there is any temp register that has this vairable inside it
          Variable isTmpExist = ctx.loaded_variables.get(p.ident_);
          Variable localVar = ctx.ctx_variables.get(p.ident_);;
          if (isTmpExist == null) {
            // We need to load the variable
            isTmpExist = ctx.GetNewTempVairableWithTheSameTypeOf(localVar);
            LLVMLoadInstruction llvmLoadInstruction = new LLVMLoadInstruction(localVar, isTmpExist);
            llvmLoadInstruction.AddNumOfSpaceForPrefix(4);
            ctx.instruction_of_ctx.add(llvmLoadInstruction);

            // We store it as a vriable that has already loaded a local vriable
            ctx.loaded_variables.put(localVar.GetVariableName(), isTmpExist);
          }

          // We have the isTmpExist as the temp register which loaded a local vriable
          Variable newTmp = ctx.GetNewTempVairableWithTheSameTypeOf(isTmpExist);
          LLVMAddInstruction llvmAddInstruction = new LLVMAddInstruction(AddType.Minus, isTmpExist, one, newTmp);
          llvmAddInstruction.AddNumOfSpaceForPrefix(4);
          ctx.instruction_of_ctx.add(llvmAddInstruction);

          // Now, we need to store it in the local val
          LLVMStoreInstruction llvmStoreInstruction = new LLVMStoreInstruction(newTmp, localVar);
          llvmStoreInstruction.AddNumOfSpaceForPrefix(4);
          ctx.instruction_of_ctx.add(llvmStoreInstruction);

          // Now remove old loaded tmp vairable
          ctx.loaded_variables.remove(localVar.GetVariableName());

          // Store the new loaded variable
          ctx.loaded_variables.put(localVar.GetVariableName(), newTmp);

          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Ret p, LLVMCodeGenCtx ctx) {
          p.expr_.accept(new ExprVisitor(), ctx);
          Variable result = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          LLVMReturnInstruction llvmReturnInstruction = new LLVMReturnInstruction(result);
          llvmReturnInstruction.AddNumOfSpaceForPrefix(4);
          ctx.instruction_of_ctx.add(llvmReturnInstruction);
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.VRet p, LLVMCodeGenCtx ctx) {
          LLVMReturnInstruction llvmReturnInstruction = new LLVMReturnInstruction();
          llvmReturnInstruction.AddNumOfSpaceForPrefix(4);
          ctx.instruction_of_ctx.add(llvmReturnInstruction);
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Cond p, LLVMCodeGenCtx ctx) {
          // We should create two label. One label is for taking the statement of if
          // the other one is for not taking the statement, and also a jumping for the 
          // statement inside the if.
          String ifStLabel = ctx.GetNewLabelWithPrefix("if.statement");
          String ifEnd = ctx.GetNewLabelWithPrefix("if.end");

          p.expr_.accept(new ExprVisitor(), ctx);
          Variable conditionResult = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          // Lets add the branch ininstruction
          LLVMJmpInstruction llvmJmpInstruction = new LLVMJmpInstruction(conditionResult, ifStLabel, ifEnd);
          llvmJmpInstruction.AddNumOfSpaceForPrefix(4);
          ctx.instruction_of_ctx.add(llvmJmpInstruction);

          // Now, we add label instruction before adding statements instructions
          LLVMLabelInstruction llvmLabelInstruction = new LLVMLabelInstruction(ifStLabel);
          llvmLabelInstruction.AddNumOfSpaceForPrefix(0);
          ctx.instruction_of_ctx.add(llvmLabelInstruction);

          p.stmt_.accept(new StmtVisitor(), ctx);

          // Now, we should add jump to the if end inside the statment
          LLVMJmpInstruction llvmJmpInstruction1 = new LLVMJmpInstruction(ifEnd);
          llvmJmpInstruction1.AddNumOfSpaceForPrefix(4);
          ctx.instruction_of_ctx.add(llvmJmpInstruction1);

          // Now, we add label instruction end after adding statements instructions
          LLVMLabelInstruction llvmLabelInstruction1 = new LLVMLabelInstruction(ifEnd);
          llvmLabelInstruction1.AddNumOfSpaceForPrefix(0);
          ctx.instruction_of_ctx.add(llvmLabelInstruction1);

          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.CondElse p, LLVMCodeGenCtx ctx) {
          // We should create three label. One label is for taking the statement of if
          // the other one is for taking the else and the last label is after the if and else.
          String ifStLabel = ctx.GetNewLabelWithPrefix("if.statement");
          String elseStLabel = ctx.GetNewLabelWithPrefix("else.statement");
          String ifEnd = ctx.GetNewLabelWithPrefix("if.end");
          
          p.expr_.accept(new ExprVisitor(), ctx);
          Variable conditionResult = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          // Lets add the branch ininstruction
          LLVMJmpInstruction llvmJmpInstruction = new LLVMJmpInstruction(conditionResult, ifStLabel, elseStLabel);
          llvmJmpInstruction.AddNumOfSpaceForPrefix(4);
          ctx.instruction_of_ctx.add(llvmJmpInstruction);

          // Now, we add label instruction before adding statements instructions
          LLVMLabelInstruction llvmLabelInstruction = new LLVMLabelInstruction(ifStLabel);
          llvmLabelInstruction.AddNumOfSpaceForPrefix(0);
          ctx.instruction_of_ctx.add(llvmLabelInstruction);

          p.stmt_1.accept(new StmtVisitor(), ctx);

          // Now, we should add jump to the if end inside if the statment
          LLVMJmpInstruction llvmJmpInstruction1 = new LLVMJmpInstruction(ifEnd);
          llvmJmpInstruction1.AddNumOfSpaceForPrefix(4);
          ctx.instruction_of_ctx.add(llvmJmpInstruction1);

          // Now, we add else label instruction before adding else statements instructions
          LLVMLabelInstruction llvmLabelInstruction1 = new LLVMLabelInstruction(elseStLabel);
          llvmLabelInstruction1.AddNumOfSpaceForPrefix(0);
          ctx.instruction_of_ctx.add(llvmLabelInstruction1);

          p.stmt_2.accept(new StmtVisitor(), ctx);

          // Now, we should add jump to the if end inside the statment
          LLVMJmpInstruction llvmJmpInstruction2 = new LLVMJmpInstruction(ifEnd);
          llvmJmpInstruction2.AddNumOfSpaceForPrefix(4);
          ctx.instruction_of_ctx.add(llvmJmpInstruction2);

          // Now, we add label instruction end after adding statements instructions
          LLVMLabelInstruction llvmLabelInstruction2 = new LLVMLabelInstruction(ifEnd);
          llvmLabelInstruction2.AddNumOfSpaceForPrefix(0);
          ctx.instruction_of_ctx.add(llvmLabelInstruction2);

          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.While p, LLVMCodeGenCtx ctx) {
          // We should create three label. One label is for re-evaluting condition of while
          // and the second one for taking the statment of while and the last one is for 
          // end of the while.
          String whileCondLabel = ctx.GetNewLabelWithPrefix("while.cond");
          String whileStLabel = ctx.GetNewLabelWithPrefix("while.st");
          String whileEndLabel = ctx.GetNewLabelWithPrefix("while.end");

          // We should add a jump to the begining of the while condition in llvm
          LLVMJmpInstruction jmpToBeginOfCondition = new LLVMJmpInstruction(whileCondLabel);
          jmpToBeginOfCondition.AddNumOfSpaceForPrefix(4);
          ctx.instruction_of_ctx.add(jmpToBeginOfCondition);

          // Now, we add label instruction before expression in order to re-evalute the
          // condition in each iteration of loop.
          LLVMLabelInstruction llvmLabelInstruction = new LLVMLabelInstruction(whileCondLabel);
          llvmLabelInstruction.AddNumOfSpaceForPrefix(0);
          ctx.instruction_of_ctx.add(llvmLabelInstruction);
 
          p.expr_.accept(new ExprVisitor(), ctx);
          Variable conditionResult = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          // We should add the branching instruction after the expression results finished.
          LLVMJmpInstruction llvmJmpInstruction = new LLVMJmpInstruction(conditionResult, whileStLabel, whileEndLabel);
          llvmJmpInstruction.AddNumOfSpaceForPrefix(4);
          ctx.instruction_of_ctx.add(llvmJmpInstruction);

          // We should add the while statement label
          LLVMLabelInstruction llvmLabelInstruction1 = new LLVMLabelInstruction(whileStLabel);
          llvmLabelInstruction1.AddNumOfSpaceForPrefix(0);
          ctx.instruction_of_ctx.add(llvmLabelInstruction1);

          p.stmt_.accept(new StmtVisitor(), ctx);

          // Now, we should jump back to the condition to re-evaluate
          LLVMJmpInstruction llvmJmpInstruction2 = new LLVMJmpInstruction(whileCondLabel);
          llvmJmpInstruction2.AddNumOfSpaceForPrefix(4);
          ctx.instruction_of_ctx.add(llvmJmpInstruction2);


          // Here we add the end label so that if the statement of while did not take
          // we continue execution
          LLVMLabelInstruction llvmLabelInstruction2 = new LLVMLabelInstruction(whileEndLabel);
          llvmLabelInstruction2.AddNumOfSpaceForPrefix(0);
          ctx.instruction_of_ctx.add(llvmLabelInstruction2);

          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.SExp p, LLVMCodeGenCtx ctx) {
          p.expr_.accept(new ExprVisitor(), ctx);
          ctx.ClearLastVariable();
          return ctx;
        }
      }
    
      public class ItemVisitor implements jlc.lib.javalette.Absyn.Item.Visitor<LLVMCodeGenCtx, LLVMCodeGenCtx> {
        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.NoInit p, LLVMCodeGenCtx ctx) {

          // Get the type of this variable from typevistor that has been set
          Variable var = ctx.GetLastVariable();
          ctx.ClearLastVariable();
          var.SetVariableName(p.ident_);

          // Now we need add alloca instruction
          LLVMAllocaInstruction llvmAllocaInstruction = new LLVMAllocaInstruction(var, var.GetVariableType());
          llvmAllocaInstruction.AddNumOfSpaceForPrefix(4);
          ctx.instruction_of_ctx.add(llvmAllocaInstruction);

          // Set so that future declaration in the same token get variable of same type
          ctx.SetLastVariable(var);

          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Init p, LLVMCodeGenCtx ctx) {
          // Get the type of this variable from typevistor that has been set
          Variable var = ctx.GetLastVariable();
          ctx.ClearLastVariable();
          var.SetVariableName(p.ident_);

          // Now we need add alloca instruction
          LLVMAllocaInstruction llvmAllocaInstruction = new LLVMAllocaInstruction(var, var.GetVariableType());
          llvmAllocaInstruction.AddNumOfSpaceForPrefix(4);
          ctx.instruction_of_ctx.add(llvmAllocaInstruction);

          // Now lets evaluate its init value
          p.expr_.accept(new ExprVisitor(), ctx);
          Variable var1 = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          // Now, we need to load the result into the variable
          LLVMStoreInstruction llvmStoreInstruction = new LLVMStoreInstruction(var1, var);
          llvmStoreInstruction.AddNumOfSpaceForPrefix(4);
          ctx.instruction_of_ctx.add(llvmStoreInstruction);

          // Set so that future declaration in the same token get variable of same type
          ctx.SetLastVariable(var);
          
          return ctx;
        }
      }
    
      public class TypeVisitor implements jlc.lib.javalette.Absyn.Type.Visitor<LLVMCodeGenCtx, LLVMCodeGenCtx> {

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Int p, LLVMCodeGenCtx ctx) {
          ctx.SetLastVariable(new IntVariable("last"));
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Doub p, LLVMCodeGenCtx ctx) {
          ctx.SetLastVariable(new DoubleVariable("last"));
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Bool p, LLVMCodeGenCtx ctx) {
          ctx.SetLastVariable(new BooleanVariable("last"));
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Void p, LLVMCodeGenCtx ctx) {
          ctx.SetLastVariable(new VoidVariable("last"));
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Fun p, LLVMCodeGenCtx ctx) {
          // TODO: I dont have any idea what is this
          p.type_.accept(new TypeVisitor(), ctx);
          for (jlc.lib.javalette.Absyn.Type x: p.listtype_) {
            x.accept(new TypeVisitor(), ctx);
          }
          return ctx;
        }
      }
    
      public class ExprVisitor implements jlc.lib.javalette.Absyn.Expr.Visitor<LLVMCodeGenCtx, LLVMCodeGenCtx> {
        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.EVar p, LLVMCodeGenCtx ctx) {
          // We first need to check if there is any temp register that has this vairable inside it
          Variable isTmpExist = ctx.loaded_variables.get(p.ident_);
          if (isTmpExist == null) {
            // We need to load the variable
            Variable localVar = ctx.ctx_variables.get(p.ident_);
            isTmpExist = ctx.GetNewTempVairableWithTheSameTypeOf(localVar);
            LLVMLoadInstruction llvmLoadInstruction = new LLVMLoadInstruction(localVar, isTmpExist);
            llvmLoadInstruction.AddNumOfSpaceForPrefix(4);
            ctx.instruction_of_ctx.add(llvmLoadInstruction);

            // We store it as a vriable that has already loaded a local vriable
            ctx.loaded_variables.put(localVar.GetVariableName(), isTmpExist);
          }

          // We have the isTmpExist as the temp register which loaded a local vriable
          ctx.SetLastVariable(isTmpExist);
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.ELitInt p, LLVMCodeGenCtx ctx) {
          Variable var = new IntVariable(p.integer_.toString());
          var.SetVariableKind(VariableKind.ConstantVariable);
          ctx.SetLastVariable(var);
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.ELitDoub p, LLVMCodeGenCtx ctx) {
          Variable var = new DoubleVariable(p.double_.toString());
          var.SetVariableKind(VariableKind.ConstantVariable);
          ctx.SetLastVariable(var);
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.ELitTrue p, LLVMCodeGenCtx ctx) {
          Variable var = new IntVariable("1");
          var.SetVariableKind(VariableKind.ConstantVariable);
          ctx.SetLastVariable(var);
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.ELitFalse p, LLVMCodeGenCtx ctx) {
          Variable var = new IntVariable("0");
          var.SetVariableKind(VariableKind.ConstantVariable);
          ctx.SetLastVariable(var);
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.EApp p, LLVMCodeGenCtx ctx) {
          
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
          LLVMFuncCallIntruction llvmFuncCallIntruction = new LLVMFuncCallIntruction(fn, tmp, args);
          llvmFuncCallIntruction.AddNumOfSpaceForPrefix(4);
          ctx.instruction_of_ctx.add(llvmFuncCallIntruction);

          // Set the result for rest of the tree
          ctx.SetLastVariable(tmp);

          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.EString p, LLVMCodeGenCtx ctx) {
          // TODO: we have to do this
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Neg p, LLVMCodeGenCtx ctx) {
          // TODO: we have to do this
          p.expr_.accept(new ExprVisitor(), ctx);
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Not p, LLVMCodeGenCtx ctx) {
          // TODO: we have to do this
          p.expr_.accept(new ExprVisitor(), ctx);
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.EMul p, LLVMCodeGenCtx ctx) {
          p.expr_1.accept(new ExprVisitor(), ctx);
          Variable var1 = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          p.mulop_.accept(new MulOpVisitor(), ctx);
          Instruction ins = ctx.GetLastInstruction();
          ctx.ClearLastInstruction();
          assert(ins instanceof LLVMMulInstruction);
          LLVMMulInstruction llvmMulInstruction = (LLVMMulInstruction) ins;

          p.expr_2.accept(new ExprVisitor(), ctx);
          Variable var2 = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          // Now lets generate the rel operation
          Variable tmp = ctx.GetNewTempVairableWithTheSameTypeOf(var2);
          llvmMulInstruction.AddNumOfSpaceForPrefix(4);
          llvmMulInstruction.SetVariables(var1, var2, tmp);
          ctx.instruction_of_ctx.add(llvmMulInstruction);

          // Set the result for rest of the tree
          ctx.SetLastVariable(tmp);

          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.EAdd p, LLVMCodeGenCtx ctx) {
          p.expr_1.accept(new ExprVisitor(), ctx);
          Variable var1 = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          p.addop_.accept(new AddOpVisitor(), ctx);
          Instruction ins = ctx.GetLastInstruction();
          ctx.ClearLastInstruction();
          assert(ins instanceof LLVMAddInstruction);
          LLVMAddInstruction llvmAddInstruction = (LLVMAddInstruction) ins;

          p.expr_2.accept(new ExprVisitor(), ctx);
          Variable var2 = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          // Now lets generate the rel operation
          Variable tmp = ctx.GetNewTempVairableWithTheSameTypeOf(var2);
          llvmAddInstruction.AddNumOfSpaceForPrefix(4);
          llvmAddInstruction.SetVariables(var1, var2, tmp);
          ctx.instruction_of_ctx.add(llvmAddInstruction);

          // Set the result for rest of the tree
          ctx.SetLastVariable(tmp);

          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.ERel p, LLVMCodeGenCtx ctx) {
          p.expr_1.accept(new ExprVisitor(), ctx);
          Variable var1 = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          p.relop_.accept(new RelOpVisitor(), ctx);
          Instruction ins = ctx.GetLastInstruction();
          ctx.ClearLastInstruction();
          assert(ins instanceof LLVMRelInstruction);
          LLVMRelInstruction llvmRelInstruction = (LLVMRelInstruction) ins;

          p.expr_2.accept(new ExprVisitor(), ctx);
          Variable var2 = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          // Now lets generate the rel operation
          Variable tmp = ctx.GetNewTempVairableWithTheSameTypeOf(var2);
          llvmRelInstruction.AddNumOfSpaceForPrefix(4);
          llvmRelInstruction.setVariables(var1, var2, tmp);
          ctx.instruction_of_ctx.add(llvmRelInstruction);

          // Set the result for rest of the tree
          ctx.SetLastVariable(tmp);

          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.EAnd p, LLVMCodeGenCtx ctx) {
          p.expr_1.accept(new ExprVisitor(), ctx);
          Variable var1 = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          p.expr_2.accept(new ExprVisitor(), ctx);
          Variable var2 = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          // Now we need to add 'and' instruction to a temp vairable
          Variable tmp = ctx.GetNewTempVairableWithTheSameTypeOf(var2);
          LLVMAndInstruction llvmAndInstruction = new LLVMAndInstruction(var1, var2, tmp);
          llvmAndInstruction.AddNumOfSpaceForPrefix(4);
          ctx.instruction_of_ctx.add(llvmAndInstruction);

          // We set the tmp so the rest of the tree can get the value
          ctx.SetLastVariable(tmp);
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.EOr p, LLVMCodeGenCtx ctx) {
          p.expr_1.accept(new ExprVisitor(), ctx);
          Variable var1 = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          p.expr_2.accept(new ExprVisitor(), ctx);
          Variable var2 = ctx.GetLastVariable();
          ctx.ClearLastVariable();

          // Now we need to add 'or' instruction to a temp vairable
          Variable tmp = ctx.GetNewTempVairableWithTheSameTypeOf(var2);
          LLVMOrInstruction llvmOrInstruction = new LLVMOrInstruction(var1, var2, tmp);
          llvmOrInstruction.AddNumOfSpaceForPrefix(4);
          ctx.instruction_of_ctx.add(llvmOrInstruction);

          // We set the tmp so the rest of the tree can get the value
          ctx.SetLastVariable(tmp);
          return ctx;
        }
      }
    
      public class AddOpVisitor implements jlc.lib.javalette.Absyn.AddOp.Visitor<LLVMCodeGenCtx, LLVMCodeGenCtx> {
        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Plus p, LLVMCodeGenCtx ctx) {
          ctx.SetLastInstruction(new LLVMAddInstruction(AddType.Plus));
          return ctx;
        }
        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Minus p, LLVMCodeGenCtx ctx) {
          ctx.SetLastInstruction(new LLVMAddInstruction(AddType.Minus));
          return ctx;
        }
      }
    
      public class MulOpVisitor implements jlc.lib.javalette.Absyn.MulOp.Visitor<LLVMCodeGenCtx, LLVMCodeGenCtx> {
        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Times p, LLVMCodeGenCtx ctx) {
          ctx.SetLastInstruction(new LLVMMulInstruction(MulType.Times));
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Div p, LLVMCodeGenCtx ctx) {
          ctx.SetLastInstruction(new LLVMMulInstruction(MulType.Div));
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Mod p, LLVMCodeGenCtx ctx) {
          ctx.SetLastInstruction(new LLVMMulInstruction(MulType.Mod));
          return ctx;
        }
      }
    
      public class RelOpVisitor implements jlc.lib.javalette.Absyn.RelOp.Visitor<LLVMCodeGenCtx, LLVMCodeGenCtx> {
        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.LTH p, LLVMCodeGenCtx ctx) {
          ctx.SetLastInstruction(new LLVMRelInstruction(RelType.LTH));
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.LE p, LLVMCodeGenCtx ctx) {
          ctx.SetLastInstruction(new LLVMRelInstruction(RelType.LE));
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.GTH p, LLVMCodeGenCtx ctx) {
          ctx.SetLastInstruction(new LLVMRelInstruction(RelType.GTH));
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.GE p, LLVMCodeGenCtx ctx) {
          ctx.SetLastInstruction(new LLVMRelInstruction(RelType.GE));
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.EQU p, LLVMCodeGenCtx ctx) {
          ctx.SetLastInstruction(new LLVMRelInstruction(RelType.EQU));
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.NE p, LLVMCodeGenCtx ctx) {
          ctx.SetLastInstruction(new LLVMRelInstruction(RelType.NE));
          return ctx;
        }
      }
}
