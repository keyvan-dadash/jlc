package jlc.main;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import jlc.lib.javalette.Absyn.ArrAss;
import jlc.lib.javalette.Absyn.ArrAssExpr;
import jlc.lib.javalette.Absyn.ArrDecr;
import jlc.lib.javalette.Absyn.ArrIncr;
import jlc.lib.javalette.Absyn.ArrIndex;
import jlc.lib.javalette.Absyn.ArrLen;
import jlc.lib.javalette.Absyn.ArrType;
import jlc.lib.javalette.Absyn.ForEach;
import jlc.lib.javalette.Absyn.NewArr;
import jlc.lib.javalette.Absyn.TypeBase;
import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.LLVM.LLVMAddInstruction;
import jlc.main.Instructions.LLVM.LLVMAllocaInstruction;
import jlc.main.Instructions.LLVM.LLVMAndInstruction;
import jlc.main.Instructions.LLVM.LLVMFuncCallIntruction;
import jlc.main.Instructions.LLVM.LLVMFuncDefenition;
import jlc.main.Instructions.LLVM.LLVMFuncDefenitionEnd;
import jlc.main.Instructions.LLVM.LLVMGlobalStringInstruction;
import jlc.main.Instructions.LLVM.LLVMJmpInstruction;
import jlc.main.Instructions.LLVM.LLVMLabelInstruction;
import jlc.main.Instructions.LLVM.LLVMLoadGlobalStringInstruction;
import jlc.main.Instructions.LLVM.LLVMLoadInstruction;
import jlc.main.Instructions.LLVM.LLVMMulInstruction;
import jlc.main.Instructions.LLVM.LLVMNegInstruction;
import jlc.main.Instructions.LLVM.LLVMNotInstruction;
import jlc.main.Instructions.LLVM.LLVMOrInstruction;
import jlc.main.Instructions.LLVM.LLVMPhiInstruction;
import jlc.main.Instructions.LLVM.LLVMRelInstruction;
import jlc.main.Instructions.LLVM.LLVMBitcastInstruction;
import jlc.main.Instructions.LLVM.LLVMGetElementPtrInstruction;
import jlc.main.Instructions.LLVM.LLVMReturnInstruction;
import jlc.main.Instructions.LLVM.LLVMStoreInstruction;
import jlc.main.Instructions.LLVM.Utils;
import jlc.main.Operations.AddType;
import jlc.main.Operations.MulType;
import jlc.main.Operations.RelType;
import jlc.main.Variables.ArrayVariable;
import jlc.main.Variables.BooleanVariable;
import jlc.main.Variables.DoubleVariable;
import jlc.main.Variables.IntVariable;
import jlc.main.Variables.StringVariable;
import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableKind;
import jlc.main.Variables.VariableType;
import jlc.main.Variables.VoidVariable;

public class LLVMCodeGeneratorVisit {
    public static final Logger logger = Logger.getLogger(LLVMCodeGeneratorVisit.class.getName());

    public class ProgVisitor implements jlc.lib.javalette.Absyn.Prog.Visitor<LLVMCodeGenCtx, LLVMCodeGenCtx> {

        public LLVMCodeGenCtx ctx;

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Program p, LLVMCodeGenCtx ctx) {
          ctx = this.ctx;

          for (jlc.lib.javalette.Absyn.TopDef x: p.listtopdef_) {
            LLVMCodeGenCtx fnCtx = LLVMCodeGenCtx.GetSubCtxWithoutVariablesAndParent(ctx);
            fnCtx.ResetCounters();
            fnCtx = x.accept(new TopDefVisitor(), fnCtx);

            // Copy instruction that has been generated for the function
            ctx.CopyInstructionsFromCtx(fnCtx);

            // Also we should copy the global variable counter
            ctx.gloabl_variable_counter = fnCtx.gloabl_variable_counter;
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
            newArg.SetVariableKind(VariableKind.TempVariable);
            arg.SetVariableKind(VariableKind.LocalVariable);
            ctx.ClearLastVariable();
          }

          Function fn = new Function();
          fn.fn_name = p.ident_;
          fn.return_var = retrun_var;
          fn.func_args = args;

          ctx.instruction_of_ctx.add(new LLVMFuncDefenition(fn));

          for (int i = 0; i < args_to_allocate.size(); i++) {
            // First, we should rename the argument
            Variable renamedArg = ctx.GetRenamedVariable(args_to_allocate.get(i));
            args_to_allocate.set(i, renamedArg);

            // Second, we should allocate the arg
            ctx.instruction_of_ctx.add(new LLVMAllocaInstruction(
                args_to_allocate.get(i),
                args_to_allocate.get(i).GetVariableType()));

            // Now we add store instruction
            Variable argVal = args.get(i);
            ctx.instruction_of_ctx.add(new LLVMStoreInstruction(argVal, args_to_allocate.get(i)));

            // we can put the arg in side the loaded variable since it is a register
            ctx.AddVariabelAsLoaded(args_to_allocate.get(i).GetVariableName(), argVal);

            // Also we should put them in ctx vairables
            ctx.AddToCtxVariable(args_to_allocate.get(i));
        }

          ctx = p.blk_.accept(new BlkVisitor(), ctx);

          // If this is a void function then the return statement might not be written
          // so we need to check and add return statement instruction to be able to compile.
          if (!ctx.is_ctx_return) {
            ctx.instruction_of_ctx.add(new LLVMReturnInstruction());
            ctx.is_ctx_return = true;
          }

          ctx.instruction_of_ctx.add(new LLVMFuncDefenitionEnd());
          return ctx;
        }

      }
    
      public class ArgVisitor implements jlc.lib.javalette.Absyn.Arg.Visitor<LLVMCodeGenCtx, LLVMCodeGenCtx> {
        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Argument p, LLVMCodeGenCtx ctx) {
          ctx = p.type_.accept(new TypeVisitor(), ctx);
          Variable arg = ctx.GetLastVariable();
          ctx.ClearLastVariable();
          arg.SetVariableName(p.ident_);
          arg.SetVariableKind(VariableKind.LocalVariable);
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
          LLVMCodeGenCtx blkCtx = LLVMCodeGenCtx.GetSubCtxWithoutVariables(ctx);
          p.blk_.accept(new BlkVisitor(), blkCtx);
          ctx.CopyInstructionsFromCtx(blkCtx);
          ctx.CopyCountersFromCtx(blkCtx);
          ctx.is_ctx_return = blkCtx.is_ctx_return;
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

            ctx.AddToCtxVariable(typeVariable);
            typeVariable.SetVariableKind(VariableKind.LocalVariable);
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
          resultVariable = ctx.EnsureLoaded(resultVariable);
          ctx.ClearLastVariable();

          // We should store resultVariable to the local variable
          Variable localVariable = ctx.GetVariableFromCtx(p.ident_);
          ctx.instruction_of_ctx.add(new LLVMStoreInstruction(resultVariable, localVariable));

          // Now, we should invalidate all the previous load instructions that loaded this local variable
          // because now it changed so that we load it again if needed.
          ctx.UnloadVariable(p.ident_);
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Incr p, LLVMCodeGenCtx ctx) {
          Variable one = new IntVariable("1");
          one.SetVariableKind(VariableKind.ConstantVariable);

          // Evaluate the expression to get the addressable location
          ctx = p.expr_.accept(new ExprVisitor(), ctx);
          Variable localVar = ctx.GetLastVariable(); 
          Variable loadedVar = ctx.EnsureLoaded(localVar);
          ctx.ClearLastVariable();

        // Now loadedValue holds the value to decrement
          Variable result = ctx.GetNewTempVairableWithTheSameTypeOf(loadedVar);
          ctx.instruction_of_ctx.add(new LLVMAddInstruction(AddType.Plus, loadedVar, one, result));

          // Store back
          if(localVar.GetVariableKind() == VariableKind.LocalVariable){
            ctx.instruction_of_ctx.add(new LLVMStoreInstruction(result, localVar));
                      // Invalidate loaded value
            ctx.UnloadVariable(localVar.GetVariableName());

            ctx.AddVariabelAsLoaded(localVar.GetVariableName(), result);
          }

          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Decr p, LLVMCodeGenCtx ctx) {
          Variable one = new IntVariable("1");
          one.SetVariableKind(VariableKind.ConstantVariable);

          // Evaluate the expression to get the addressable location
          ctx = p.expr_.accept(new ExprVisitor(), ctx);
          Variable localVar = ctx.GetLastVariable(); 
          Variable loadedVar = ctx.EnsureLoaded(localVar);
          ctx.ClearLastVariable();

        //Now loadedValue holds the value to decrement
          Variable result = ctx.GetNewTempVairableWithTheSameTypeOf(loadedVar);
          ctx.instruction_of_ctx.add(new LLVMAddInstruction(AddType.Minus, loadedVar, one, result));

          if(localVar.GetVariableKind() == VariableKind.LocalVariable){
            ctx.instruction_of_ctx.add(new LLVMStoreInstruction(result, localVar));

            ctx.UnloadVariable(localVar.GetVariableName());

            ctx.AddVariabelAsLoaded(localVar.GetVariableName(), result);
          }

          return ctx;
        }


        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Ret p, LLVMCodeGenCtx ctx) {
          p.expr_.accept(new ExprVisitor(), ctx);
          Variable result = ctx.GetLastVariable();
          result = ctx.EnsureLoaded(result);
          ctx.ClearLastVariable();

          ctx.instruction_of_ctx.add(new LLVMReturnInstruction(result));
          ctx.is_ctx_return = true;
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.VRet p, LLVMCodeGenCtx ctx) {
          ctx.instruction_of_ctx.add(new LLVMReturnInstruction());
          ctx.is_ctx_return = true;
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Cond p, LLVMCodeGenCtx ctx) {
          List<String> labels = ctx.GetNewLabelWithPrefix("if.statement", "if.end");
          String ifStLabel = labels.get(0);
          String ifEnd = labels.get(1);

          p.expr_.accept(new ExprVisitor(), ctx);
          Variable conditionResult = ctx.GetLastVariable();
          conditionResult = ctx.EnsureLoaded(conditionResult);
          ctx.ClearLastVariable();

          // Lets add the branch ininstruction
          ctx.instruction_of_ctx.add(new LLVMJmpInstruction(conditionResult, ifStLabel, ifEnd));

          // Now, we add label instruction before adding statements instructions
          ctx.instruction_of_ctx.add(new LLVMLabelInstruction(ifStLabel));

          LLVMCodeGenCtx ifCtx = LLVMCodeGenCtx.GetSubCtxWithVariables(ctx);
          p.stmt_.accept(new StmtVisitor(), ifCtx);
          ctx.CopyInstructionsFromCtx(ifCtx);
          ctx.CopyCountersFromCtx(ifCtx);

          if (!ifCtx.is_ctx_return) {
            // Now, we should add jump to the if end inside the statment
            ctx.instruction_of_ctx.add(new LLVMJmpInstruction(ifEnd));
          }

          // Now, we add label instruction end after adding statements instructions
          ctx.instruction_of_ctx.add(new LLVMLabelInstruction(ifEnd));

          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.CondElse p, LLVMCodeGenCtx ctx) {
          // We should create three label. One label is for taking the statement of if
          // the other one is for taking the else and the last label is after the if and else.
          List<String> labels = ctx.GetNewLabelWithPrefix("if.statement", "else.statement", "if.end");
          String ifStLabel = labels.get(0);
          String elseStLabel = labels.get(1);
          String ifEnd = labels.get(2);
          
          p.expr_.accept(new ExprVisitor(), ctx);
          Variable conditionResult = ctx.GetLastVariable();
          conditionResult = ctx.EnsureLoaded(conditionResult);
          ctx.ClearLastVariable();

          // Lets add the branch ininstruction
          ctx.instruction_of_ctx.add(new LLVMJmpInstruction(conditionResult, ifStLabel, elseStLabel));

          // Now, we add label instruction before adding statements instructions
          ctx.instruction_of_ctx.add(new LLVMLabelInstruction(ifStLabel));

          LLVMCodeGenCtx ifCtx = LLVMCodeGenCtx.GetSubCtxWithVariables(ctx);
          p.stmt_1.accept(new StmtVisitor(), ifCtx);
          ctx.CopyInstructionsFromCtx(ifCtx);
          ctx.CopyCountersFromCtx(ifCtx);

          if (!ifCtx.is_ctx_return) {
            // Now, we should add jump to the if end inside if the statment
            ctx.instruction_of_ctx.add(new LLVMJmpInstruction(ifEnd));
          }

          // Now, we add else label instruction before adding else statements instructions
          ctx.instruction_of_ctx.add(new LLVMLabelInstruction(elseStLabel));

          LLVMCodeGenCtx elseCtx = LLVMCodeGenCtx.GetSubCtxWithVariables(ctx);
          p.stmt_2.accept(new StmtVisitor(), elseCtx);
          ctx.CopyInstructionsFromCtx(elseCtx);
          ctx.CopyCountersFromCtx(elseCtx);

          if (!elseCtx.is_ctx_return) {
            // Now, we should add jump to the if end inside the statment
            ctx.instruction_of_ctx.add(new LLVMJmpInstruction(ifEnd));
          }

          if (!ifCtx.is_ctx_return || !elseCtx.is_ctx_return) {
            // Now, we add label instruction end after adding statements instructions
            ctx.instruction_of_ctx.add(new LLVMLabelInstruction(ifEnd));
          }

          ctx.is_ctx_return = ifCtx.is_ctx_return && elseCtx.is_ctx_return;

          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.While p, LLVMCodeGenCtx ctx) {
          List<String> labels = ctx.GetNewLabelWithPrefix("while.cond", "while.st", "while.end");
          String whileCondLabel = labels.get(0);
          String whileStLabel = labels.get(1);
          String whileEndLabel = labels.get(2);

          // We should add a jump to the begining of the while condition in llvm
          ctx.instruction_of_ctx.add(new LLVMJmpInstruction(whileCondLabel));

          // Now, we add label instruction before expression in order to re-evalute the
          // condition in each iteration of loop.
          ctx.instruction_of_ctx.add(new LLVMLabelInstruction(whileCondLabel));
 
          p.expr_.accept(new ExprVisitor(), ctx);
          Variable conditionResult = ctx.GetLastVariable();
          conditionResult = ctx.EnsureLoaded(conditionResult);
          ctx.ClearLastVariable();

          // We should add the branching instruction after the expression results finished.
          ctx.instruction_of_ctx.add(new LLVMJmpInstruction(conditionResult, whileStLabel, whileEndLabel));

          // We should add the while statement label
          ctx.instruction_of_ctx.add(new LLVMLabelInstruction(whileStLabel));

          p.stmt_.accept(new StmtVisitor(), ctx);

          // Now, we should jump back to the condition to re-evaluate
          ctx.instruction_of_ctx.add(new LLVMJmpInstruction(whileCondLabel));


          // Here we add the end label so that if the statement of while did not take
          // we continue execution
          ctx.instruction_of_ctx.add(new LLVMLabelInstruction(whileEndLabel));

          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.SExp p, LLVMCodeGenCtx ctx) {
          p.expr_.accept(new ExprVisitor(), ctx);
          Variable result = ctx.GetLastVariable();
          result = ctx.EnsureLoaded(result);
          ctx.ClearLastVariable();
          return ctx;
        }

      @Override
      public LLVMCodeGenCtx visit(ArrAss p, LLVMCodeGenCtx ctx) {
          // Evaluate the array variable (should yield an ArrayVariable)
          Variable arrVar = ctx.GetVariableFromCtx(p.ident_);
          VariableType elemType = ((ArrayVariable) arrVar).GetArrayType().GetVariableType();

          //Load the array pointer (i8*)
          Variable arrPtr = ctx.EnsureLoaded(arrVar);

          // Evaluate the index expression
          p.expr_1.accept(new ExprVisitor(), ctx);
          Variable idxVar = ctx.GetLastVariable();
          idxVar = ctx.EnsureLoaded(idxVar);
          ctx.ClearLastVariable();

          // evaluate the value to assign
          p.expr_2.accept(new ExprVisitor(), ctx);
          Variable valueVar = ctx.GetLastVariable();
          valueVar = ctx.EnsureLoaded(valueVar);
          ctx.ClearLastVariable();

          // Bitcast the i8* to thecorrect element pointer type
          Variable elemPtrTypeVar;
          switch (elemType) {
              case Int:
                  elemPtrTypeVar = ctx.GetNewTempVairableWithTheSameTypeOf(new IntVariable(""));
                  ctx.instruction_of_ctx.add(
                      new LLVMBitcastInstruction(arrPtr, elemPtrTypeVar, "i32*")
                  );
                  break;
              case Double:
                  elemPtrTypeVar = ctx.GetNewTempVairableWithTheSameTypeOf(new DoubleVariable(""));
                  ctx.instruction_of_ctx.add(
                      new LLVMBitcastInstruction(arrPtr, elemPtrTypeVar, "double*")
                  );
                  break;
              case Boolean:
                  elemPtrTypeVar = ctx.GetNewTempVairableWithTheSameTypeOf(new BooleanVariable(""));
                  ctx.instruction_of_ctx.add(
                      new LLVMBitcastInstruction(arrPtr, elemPtrTypeVar, "i8*")
                  );
                  break;
              default:
                  throw new RuntimeException("ArrAss: Unsupported element type");
          }

          // GEP to get the address of the element
          Variable gepResult = ctx.GetNewTempVairableWithTheSameTypeOf(elemPtrTypeVar);
          ctx.instruction_of_ctx.add(
              new LLVMGetElementPtrInstruction(elemPtrTypeVar, idxVar, gepResult, elemType)
          );

          // Store the value at that address
          ctx.instruction_of_ctx.add(
              new LLVMStoreInstruction(valueVar, gepResult)
          );

          return ctx;
      }

      @Override
      public LLVMCodeGenCtx visit(ArrAssExpr p, LLVMCodeGenCtx ctx) {
          // Evaluate the array expression (Expr6)
          p.expr_1.accept(new ExprVisitor(), ctx);
          Variable arrVar = ctx.GetLastVariable();
          arrVar = ctx.EnsureLoaded(arrVar);
          ctx.ClearLastVariable();

          //Evaluate the index expression
          p.expr_2.accept(new ExprVisitor(), ctx);
          Variable idxVar = ctx.GetLastVariable();
          idxVar = ctx.EnsureLoaded(idxVar);
          ctx.ClearLastVariable();

          // Evaluate the value to assign
          p.expr_3.accept(new ExprVisitor(), ctx);
          Variable valueVar = ctx.GetLastVariable();
          valueVar = ctx.EnsureLoaded(valueVar);
          ctx.ClearLastVariable();

          // Determine the element type (from the array variable)
          VariableType elemType;
          if (arrVar instanceof ArrayVariable) {
              elemType = ((ArrayVariable) arrVar).GetArrayType().GetVariableType();
          } else {
              // If your ExprVisitor doesn't return ArrayVariable, you may need to track type info elsewhere
              throw new RuntimeException("ArrAssExpr: arrVar is not an ArrayVariable");
          }

          // Bitcast the i8* to the correct element pointer type
          Variable elemPtrTypeVar;
          switch (elemType) {
              case Int:
                  elemPtrTypeVar = ctx.GetNewTempVairableWithTheSameTypeOf(new IntVariable(""));
                  ctx.instruction_of_ctx.add(
                      new LLVMBitcastInstruction(arrVar, elemPtrTypeVar, "i32*")
                  );
                  break;
              case Double:
                  elemPtrTypeVar = ctx.GetNewTempVairableWithTheSameTypeOf(new DoubleVariable(""));
                  ctx.instruction_of_ctx.add(
                      new LLVMBitcastInstruction(arrVar, elemPtrTypeVar, "double*")
                  );
                  break;
              case Boolean:
                  elemPtrTypeVar = ctx.GetNewTempVairableWithTheSameTypeOf(new BooleanVariable(""));
                  ctx.instruction_of_ctx.add(
                      new LLVMBitcastInstruction(arrVar, elemPtrTypeVar, "i8*")
                  );
                  break;
              default:
                  throw new RuntimeException("ArrAssExpr: Unsupported element type");
          }

          // GEP to get the address of the element
          Variable gepResult = ctx.GetNewTempVairableWithTheSameTypeOf(elemPtrTypeVar);
          ctx.instruction_of_ctx.add(
              new LLVMGetElementPtrInstruction(elemPtrTypeVar, idxVar, gepResult, elemType)
          );

          // Store the value at that address
          ctx.instruction_of_ctx.add(
              new LLVMStoreInstruction(valueVar, gepResult)
          );

          return ctx;
      }

        @Override
        public LLVMCodeGenCtx visit(ForEach p, LLVMCodeGenCtx ctx) {
            // Evaluate the array expression
            p.expr_.accept(new ExprVisitor(), ctx);
            Variable arrVar = ctx.GetLastVariable();
            arrVar = ctx.EnsureLoaded(arrVar);
            ctx.ClearLastVariable();

            // Get the element type
            VariableType elemType;
            if (arrVar instanceof ArrayVariable) {
                elemType = ((ArrayVariable) arrVar).GetArrayType().GetVariableType();
            } else {
                throw new RuntimeException("ForEach: arrVar is not an ArrayVariable");
            }

            // Get array length
            Variable lenVar = ctx.GetNewTempVairableWithTheSameTypeOf(new IntVariable(""));
            ctx.instruction_of_ctx.add(
                new LLVMFuncCallIntruction("array_length", lenVar, arrVar)
            );

            // Allocate and initialize loop index
            Variable idxVar = ctx.GetNewTempVairableWithTheSameTypeOf(new IntVariable(""));
            ctx.instruction_of_ctx.add(new LLVMAllocaInstruction(idxVar, VariableType.Int));
            Variable zero = new IntVariable("0");
            zero.SetVariableKind(VariableKind.ConstantVariable);
            ctx.instruction_of_ctx.add(new LLVMStoreInstruction(zero, idxVar));

            // Create labels
            List<String> labels = ctx.GetNewLabelWithPrefix("foreach.cond", "foreach.body", "foreach.inc", "foreach.end");
            String condLabel = labels.get(0);
            String bodyLabel = labels.get(1);
            String incLabel = labels.get(2);
            String endLabel = labels.get(3);

            // Jump to condition
            ctx.instruction_of_ctx.add(new LLVMJmpInstruction(condLabel));

            // Condition block
            ctx.instruction_of_ctx.add(new LLVMLabelInstruction(condLabel));
            Variable idxLoaded = ctx.GetNewTempVairableWithTheSameTypeOf(new IntVariable(""));
            ctx.instruction_of_ctx.add(new LLVMLoadInstruction(idxVar, idxLoaded));
            Variable condVar = ctx.GetNewTempVairableWithTheSameTypeOf(new BooleanVariable(""));
            ctx.instruction_of_ctx.add(new LLVMRelInstruction(RelType.LTH, idxLoaded, lenVar, condVar));
            ctx.instruction_of_ctx.add(new LLVMJmpInstruction(condVar, bodyLabel, endLabel));

            // Body block
            ctx.instruction_of_ctx.add(new LLVMLabelInstruction(bodyLabel));
            // Bitcast arrVar to element pointer type
            Variable elemPtrTypeVar;
            switch (elemType) {
                case Int:
                    elemPtrTypeVar = ctx.GetNewTempVairableWithTheSameTypeOf(new IntVariable(""));
                    ctx.instruction_of_ctx.add(new LLVMBitcastInstruction(arrVar, elemPtrTypeVar, "i32*"));
                    break;
                case Double:
                    elemPtrTypeVar = ctx.GetNewTempVairableWithTheSameTypeOf(new DoubleVariable(""));
                    ctx.instruction_of_ctx.add(new LLVMBitcastInstruction(arrVar, elemPtrTypeVar, "double*"));
                    break;
                case Boolean:
                    elemPtrTypeVar = ctx.GetNewTempVairableWithTheSameTypeOf(new BooleanVariable(""));
                    ctx.instruction_of_ctx.add(new LLVMBitcastInstruction(arrVar, elemPtrTypeVar, "i8*"));
                    break;
                default:
                    throw new RuntimeException("ForEach: Unsupported element type");
            }
            // GEP to array[i]
            Variable gepResult = ctx.GetNewTempVairableWithTheSameTypeOf(elemPtrTypeVar);
            ctx.instruction_of_ctx.add(new LLVMGetElementPtrInstruction(elemPtrTypeVar, idxLoaded, gepResult, elemType));
            // Load array[i]
            Variable elemVal = ctx.GetNewTempVairableWithTheSameTypeOf(
                elemType == VariableType.Int ? new IntVariable("") :
                elemType == VariableType.Double ? new DoubleVariable("") :
                new BooleanVariable("")
            );
            ctx.instruction_of_ctx.add(new LLVMLoadInstruction(gepResult, elemVal));
            // Allocate and store loop variable
            Variable loopVar = elemType == VariableType.Int
                ? new IntVariable(p.ident_)
                : elemType == VariableType.Double
                    ? new DoubleVariable(p.ident_)
                    : new BooleanVariable(p.ident_);
            loopVar.SetVariableKind(VariableKind.LocalVariable);
            loopVar = ctx.GetRenamedVariable(loopVar);
            ctx.instruction_of_ctx.add(new LLVMAllocaInstruction(loopVar, elemType));
            ctx.instruction_of_ctx.add(new LLVMStoreInstruction(elemVal, loopVar));
            ctx.AddToCtxVariable(loopVar);

            // Emit the body
            p.stmt_.accept(new StmtVisitor(), ctx);

            // Increment block
            ctx.instruction_of_ctx.add(new LLVMJmpInstruction(incLabel));
            ctx.instruction_of_ctx.add(new LLVMLabelInstruction(incLabel));
            Variable idxInc = ctx.GetNewTempVairableWithTheSameTypeOf(new IntVariable(""));
            Variable one = new IntVariable("1");
            one.SetVariableKind(VariableKind.ConstantVariable);
            ctx.instruction_of_ctx.add(new LLVMAddInstruction(AddType.Plus, idxLoaded, one, idxInc));
            ctx.instruction_of_ctx.add(new LLVMStoreInstruction(idxInc, idxVar));
            ctx.instruction_of_ctx.add(new LLVMJmpInstruction(condLabel));

            // End block
            ctx.instruction_of_ctx.add(new LLVMLabelInstruction(endLabel));

            return ctx;
        }

        @Override
        public LLVMCodeGenCtx visit(ArrIncr p, LLVMCodeGenCtx ctx) {
            // Get the array variable and its element type
            Variable arrVar = ctx.GetVariableFromCtx(p.ident_);
            VariableType elemType = ((ArrayVariable) arrVar).GetArrayType().GetVariableType();

            // Load the array pointer (i8*)
            Variable arrPtr = ctx.EnsureLoaded(arrVar);

            // Evaluate the index expression
            p.expr_.accept(new ExprVisitor(), ctx);
            Variable idxVar = ctx.GetLastVariable();
            idxVar = ctx.EnsureLoaded(idxVar);
            ctx.ClearLastVariable();

            // Bitcast to the correct element pointer type
            Variable elemPtrTypeVar;
            switch (elemType) {
                case Int:
                    elemPtrTypeVar = ctx.GetNewTempVairableWithTheSameTypeOf(new IntVariable(""));
                    ctx.instruction_of_ctx.add(
                        new LLVMBitcastInstruction(arrPtr, elemPtrTypeVar, "i32*")
                    );
                    break;
                case Double:
                    elemPtrTypeVar = ctx.GetNewTempVairableWithTheSameTypeOf(new DoubleVariable(""));
                    ctx.instruction_of_ctx.add(
                        new LLVMBitcastInstruction(arrPtr, elemPtrTypeVar, "double*")
                    );
                    break;
                case Boolean:
                    elemPtrTypeVar = ctx.GetNewTempVairableWithTheSameTypeOf(new BooleanVariable(""));
                    ctx.instruction_of_ctx.add(
                        new LLVMBitcastInstruction(arrPtr, elemPtrTypeVar, "i8*")
                    );
                    break;
                default:
                    throw new RuntimeException("ArrDecr: Unsupported element type");
            }

            // GEP to get the address of the element
            Variable gepResult = ctx.GetNewTempVairableWithTheSameTypeOf(elemPtrTypeVar);
            ctx.instruction_of_ctx.add(
                new LLVMGetElementPtrInstruction(elemPtrTypeVar, idxVar, gepResult, elemType)
            );

            // Load the current value
            Variable loaded = ctx.GetNewTempVairableWithTheSameTypeOf(
                elemType == VariableType.Int ? new IntVariable("") :
                elemType == VariableType.Double ? new DoubleVariable("") :
                new BooleanVariable("")
            );
            ctx.instruction_of_ctx.add(
                new LLVMLoadInstruction(gepResult, loaded)
            );

            //  Subtract 1 (or 1.0 for double)
            Variable one = elemType == VariableType.Double
                ? new DoubleVariable("1.0")
                : new IntVariable("1");
            one.SetVariableKind(VariableKind.ConstantVariable);

            Variable result = ctx.GetNewTempVairableWithTheSameTypeOf(loaded);
            ctx.instruction_of_ctx.add(
                new LLVMAddInstruction(AddType.Plus, loaded, one, result)
            );

            // Store the result back
            ctx.instruction_of_ctx.add(
                new LLVMStoreInstruction(result, gepResult)
            );

            return ctx;
        }

        @Override
        public LLVMCodeGenCtx visit(ArrDecr p, LLVMCodeGenCtx ctx) {
            // Get the array variable and its element type
            Variable arrVar = ctx.GetVariableFromCtx(p.ident_);
            VariableType elemType = ((ArrayVariable) arrVar).GetArrayType().GetVariableType();

            //  Load the array pointer (i8*)
            Variable arrPtr = ctx.EnsureLoaded(arrVar);

            // Evaluate the index expression
            p.expr_.accept(new ExprVisitor(), ctx);
            Variable idxVar = ctx.GetLastVariable();
            idxVar = ctx.EnsureLoaded(idxVar);
            ctx.ClearLastVariable();

            //Bitcast to the correct element pointer type
            Variable elemPtrTypeVar;
            switch (elemType) {
                case Int:
                    elemPtrTypeVar = ctx.GetNewTempVairableWithTheSameTypeOf(new IntVariable(""));
                    ctx.instruction_of_ctx.add(
                        new LLVMBitcastInstruction(arrPtr, elemPtrTypeVar, "i32*")
                    );
                    break;
                case Double:
                    elemPtrTypeVar = ctx.GetNewTempVairableWithTheSameTypeOf(new DoubleVariable(""));
                    ctx.instruction_of_ctx.add(
                        new LLVMBitcastInstruction(arrPtr, elemPtrTypeVar, "double*")
                    );
                    break;
                case Boolean:
                    elemPtrTypeVar = ctx.GetNewTempVairableWithTheSameTypeOf(new BooleanVariable(""));
                    ctx.instruction_of_ctx.add(
                        new LLVMBitcastInstruction(arrPtr, elemPtrTypeVar, "i8*")
                    );
                    break;
                default:
                    throw new RuntimeException("ArrDecr: Unsupported element type");
            }

            // GEP to get the address of the element
            Variable gepResult = ctx.GetNewTempVairableWithTheSameTypeOf(elemPtrTypeVar);
            ctx.instruction_of_ctx.add(
                new LLVMGetElementPtrInstruction(elemPtrTypeVar, idxVar, gepResult, elemType)
            );

            // Load the current value
            Variable loaded = ctx.GetNewTempVairableWithTheSameTypeOf(
                elemType == VariableType.Int ? new IntVariable("") :
                elemType == VariableType.Double ? new DoubleVariable("") :
                new BooleanVariable("")
            );
            ctx.instruction_of_ctx.add(
                new LLVMLoadInstruction(gepResult, loaded)
            );

            // Subtract 1 (or 1.0 for double)
            Variable one = elemType == VariableType.Double
                ? new DoubleVariable("1.0")
                : new IntVariable("1");
            one.SetVariableKind(VariableKind.ConstantVariable);

            Variable result = ctx.GetNewTempVairableWithTheSameTypeOf(loaded);
            ctx.instruction_of_ctx.add(
                new LLVMAddInstruction(AddType.Minus, loaded, one, result)
            );

            // Store the result back
            ctx.instruction_of_ctx.add(
                new LLVMStoreInstruction(result, gepResult)
            );

            return ctx;
        }
      }
    
      public class ItemVisitor implements jlc.lib.javalette.Absyn.Item.Visitor<LLVMCodeGenCtx, LLVMCodeGenCtx> {
        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.NoInit p, LLVMCodeGenCtx ctx) {

          // Get the type of this variable from typevistor that has been set
          Variable var = ctx.GetLastVariable();
          ctx.ClearLastVariable();
          var.SetVariableName(p.ident_);
          var.SetVariableKind(VariableKind.LocalVariable);

          var = ctx.GetRenamedVariable(var);
          ctx.instruction_of_ctx.add(new LLVMAllocaInstruction(var, var.GetVariableType()));

          // Now, we should intitlize the variable by ourself
          ctx.instruction_of_ctx.add(new LLVMStoreInstruction(Utils.GetDefaultValueOfVariableType(var), var));
          
          if (var.GetVariableType() == VariableType.Array) {
            // Determine the allocation function
            Variable arrType = ((ArrayVariable) var).GetArrayType();
            String allocFunc;
            switch (arrType.GetVariableType()) {
                case Int:     allocFunc = "alloc_array_i32"; break;
                case Double:  allocFunc = "alloc_array_double"; break;
                case Boolean: allocFunc = "alloc_array_i8"; break;
                default: 
                    throw new RuntimeException("Unsupported array type: ");
            }

            // Allocate the array: call the runtime function, result is i8*
            Variable arrPtr = ctx.GetNewTempVairableWithTheSameTypeOf(new ArrayVariable("arr", arrType.GetVariableType()));
            Variable zero = new IntVariable("0");
            zero.SetVariableKind(VariableKind.ConstantVariable);
            ctx.instruction_of_ctx.add(new LLVMFuncCallIntruction(allocFunc, arrPtr, zero));
            // Store the array pointer in the variable
            ctx.instruction_of_ctx.add(new LLVMStoreInstruction(arrPtr, var));
          }
          // Set so that future declaration in the same token get variable of same type
          ctx.SetLastVariable(var);

          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Init p, LLVMCodeGenCtx ctx) {
          // Get the type of this variable from typevistor that has been set
          Variable var = ctx.GetLastVariable();
          ctx.ClearLastVariable();
          var.SetVariableName(p.ident_);
          var.SetVariableKind(VariableKind.LocalVariable);
          
          // Lets renamed the variable
          var = ctx.GetRenamedVariable(var);

          // Now we need add alloca instruction
          ctx.instruction_of_ctx.add(new LLVMAllocaInstruction(var, var.GetVariableType()));

          // Now lets evaluate its init value
          p.expr_.accept(new ExprVisitor(), ctx);
          Variable var1 = ctx.GetLastVariable();
          var1 = ctx.EnsureLoaded(var1);

          ctx.ClearLastVariable();

          // Now, we need to load the result into the variable
          ctx.instruction_of_ctx.add(new LLVMStoreInstruction(var1, var));

          // Set so that future declaration in the same token get variable of same type
          ctx.SetLastVariable(var);
          
          return ctx;
        }
      }
    
      public class TypeVisitor implements jlc.lib.javalette.Absyn.Type.Visitor<LLVMCodeGenCtx, LLVMCodeGenCtx> {
        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Fun p, LLVMCodeGenCtx ctx) {
          // TODO: I dont have any idea what is this
          p.type_.accept(new TypeVisitor(), ctx);
          for (jlc.lib.javalette.Absyn.Type x: p.listtype_) {
            x.accept(new TypeVisitor(), ctx);
          }
          return ctx;
        }

        @Override
        public LLVMCodeGenCtx visit(TypeBase p, LLVMCodeGenCtx ctx) {
          p.basetype_.accept(new BaseTypeVisitor(), ctx);
          return ctx;
        }

        @Override
        public LLVMCodeGenCtx visit(ArrType p, LLVMCodeGenCtx ctx) {
          ctx = p.type_.accept(new TypeVisitor(), ctx);
          Variable arrType = ctx.GetLastVariable();  
          ctx.SetLastVariable(new ArrayVariable("last", arrType.GetVariableType()));
          return ctx;
        }
      }
      public class BaseTypeVisitor implements jlc.lib.javalette.Absyn.BaseType.Visitor<LLVMCodeGenCtx, LLVMCodeGenCtx> {

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
      }
      public class ExprVisitor implements jlc.lib.javalette.Absyn.Expr.Visitor<LLVMCodeGenCtx, LLVMCodeGenCtx> {
        private boolean IsNewExper = true;

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.EVar p, LLVMCodeGenCtx ctx) {
            Variable var = ctx.GetVariableFromCtx(p.ident_);
            ctx.SetLastVariable(var);
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
          Variable var = new BooleanVariable("1");
          var.SetVariableKind(VariableKind.ConstantVariable);
          ctx.SetLastVariable(var);
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.ELitFalse p, LLVMCodeGenCtx ctx) {
          Variable var = new BooleanVariable("0");
          var.SetVariableKind(VariableKind.ConstantVariable);
          ctx.SetLastVariable(var);
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.EApp p, LLVMCodeGenCtx ctx) {
          
          List<Variable> args = new ArrayList<>();
          for (jlc.lib.javalette.Absyn.Expr x: p.listexpr_) {
            x.accept(new ExprVisitor(), ctx);
            Variable arg = ctx.GetLastVariable();
            arg = ctx.EnsureLoaded(arg);
            ctx.ClearLastVariable();
            args.add(arg);
          }

          Function fn = ctx.functions.get(p.ident_);
          // Lets create a tmp variable same type of the return value of function
          Variable tmp = ctx.GetNewTempVairableWithTheSameTypeOf(fn.return_var);

          // Here we should add new temp variables in case we are trying to pass string
          for (int i = 0; i < args.size(); i++) {
            if (args.get(i).GetVariableType() == VariableType.String) {
                // We need to cast and generate new temp variable for it.
                Variable tempVariable = ctx.GetNewTempVairableWithTheSameTypeOf(args.get(i));
                String strContent = ctx.global_strings.get(args.get(i).GetVariableName());
                ctx.instruction_of_ctx.add(new LLVMLoadGlobalStringInstruction(tempVariable, args.get(i).GetVariableName(), strContent));
                args.set(i, tempVariable);
            }
          }

          ctx.instruction_of_ctx.add(new LLVMFuncCallIntruction(fn, tmp, args));

          // Set the result for rest of the tree
          ctx.SetLastVariable(tmp);
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.EString p, LLVMCodeGenCtx ctx) {
          // First, we should get a global variable
          Variable gVariable = ctx.GetNewGlobalVairableWithTheSameTypeOf(new StringVariable(""));
          gVariable.SetVariableKind(VariableKind.GlobalVariable);

          // Lets add global instruction
          ctx.global_instructions.add(new LLVMGlobalStringInstruction(gVariable.GetVariableName(), p.string_));

          ctx.global_strings.put(gVariable.GetVariableName(), p.string_);
          ctx.SetLastVariable(gVariable);
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Neg p, LLVMCodeGenCtx ctx) {
          p.expr_.accept(new ExprVisitor(), ctx);
          Variable result = ctx.GetLastVariable();
          result = ctx.EnsureLoaded(result);
          ctx.ClearLastVariable();

          // Lets get new temp variable
          Variable tempVariable = ctx.GetNewTempVairableWithTheSameTypeOf(result);
          ctx.instruction_of_ctx.add(new LLVMNegInstruction(result, tempVariable));

          // Set the result now
          ctx.SetLastVariable(tempVariable);
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.Not p, LLVMCodeGenCtx ctx) {
          p.expr_.accept(new ExprVisitor(), ctx);
          Variable result = ctx.GetLastVariable();
          result = ctx.EnsureLoaded(result);
          ctx.ClearLastVariable();

          // Lets get new temp variable
          Variable tempVariable = ctx.GetNewTempVairableWithTheSameTypeOf(result);
          ctx.instruction_of_ctx.add(new LLVMNotInstruction(result, tempVariable));

          // Set the result now
          ctx.SetLastVariable(tempVariable);
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.EMul p, LLVMCodeGenCtx ctx) {
          p.expr_1.accept(new ExprVisitor(), ctx);
          Variable var1 = ctx.GetLastVariable();
          var1 = ctx.EnsureLoaded(var1);
          ctx.ClearLastVariable();

          p.mulop_.accept(new MulOpVisitor(), ctx);
          Instruction ins = ctx.GetLastInstruction();
          ctx.ClearLastInstruction();
          assert(ins instanceof LLVMMulInstruction);
          LLVMMulInstruction llvmMulInstruction = (LLVMMulInstruction) ins;

          p.expr_2.accept(new ExprVisitor(), ctx);
          Variable var2 = ctx.GetLastVariable();
          var2 = ctx.EnsureLoaded(var2);
          ctx.ClearLastVariable();

          // Now lets generate the rel operation
          Variable tmp = ctx.GetNewTempVairableWithTheSameTypeOf(var2);
          llvmMulInstruction.SetVariables(var1, var2, tmp);
          ctx.instruction_of_ctx.add(llvmMulInstruction);

          // Set the result for rest of the tree
          ctx.SetLastVariable(tmp);

          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.EAdd p, LLVMCodeGenCtx ctx) {
          p.expr_1.accept(new ExprVisitor(), ctx);
          Variable var1 = ctx.GetLastVariable();
          var1 = ctx.EnsureLoaded(var1);
          ctx.ClearLastVariable();

          p.addop_.accept(new AddOpVisitor(), ctx);
          Instruction ins = ctx.GetLastInstruction();
          ctx.ClearLastInstruction();
          assert(ins instanceof LLVMAddInstruction);
          LLVMAddInstruction llvmAddInstruction = (LLVMAddInstruction) ins;

          p.expr_2.accept(new ExprVisitor(), ctx);
          Variable var2 = ctx.GetLastVariable();
          var2 = ctx.EnsureLoaded(var2);
          ctx.ClearLastVariable();

          // Now lets generate the rel operation
          Variable tmp = ctx.GetNewTempVairableWithTheSameTypeOf(var2);
          llvmAddInstruction.SetVariables(var1, var2, tmp);
          ctx.instruction_of_ctx.add(llvmAddInstruction);

          // Set the result for rest of the tree
          ctx.SetLastVariable(tmp);

          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.ERel p, LLVMCodeGenCtx ctx) {
          p.expr_1.accept(new ExprVisitor(), ctx);
          Variable var1 = ctx.GetLastVariable();
          var1 = ctx.EnsureLoaded(var1);
          ctx.ClearLastVariable();

          p.relop_.accept(new RelOpVisitor(), ctx);
          Instruction ins = ctx.GetLastInstruction();
          ctx.ClearLastInstruction();
          assert(ins instanceof LLVMRelInstruction);
          LLVMRelInstruction llvmRelInstruction = (LLVMRelInstruction) ins;

          p.expr_2.accept(new ExprVisitor(), ctx);
          Variable var2 = ctx.GetLastVariable();
          var2 = ctx.EnsureLoaded(var2);
          ctx.ClearLastVariable();

          // Now lets generate the rel operation
          // result of rel operation is always boolean
          Variable tmp = ctx.GetNewTempVairableWithTheSameTypeOf(new BooleanVariable(""));
          llvmRelInstruction.SetVariables(var1, var2, tmp);
          ctx.instruction_of_ctx.add(llvmRelInstruction);

          // Set the result for rest of the tree
          ctx.SetLastVariable(tmp);

          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.EAnd p, LLVMCodeGenCtx ctx) {
          if (IsNewExper) {
            // remove labels of last expressions
            IsNewExper = false;
            ctx.jmp_labels.clear();
          }

          // Before evaluating first part of and we should jump to a stupid label so that phi 
          // instruction can decide later based on it
          List<String> labels = ctx.GetNewLabelWithPrefix("and.first.true", "and.first.false", "and.end");
          String andFirstPartTrue = labels.get(0);
          String andFirstPartFalse = labels.get(1);
          String andEnd = labels.get(2);

          // Now we evaluate the first part of and
          p.expr_1.accept(new ExprVisitor(), ctx);
          Variable var1 = ctx.GetLastVariable();
          var1 = ctx.EnsureLoaded(var1);
          ctx.ClearLastVariable();

          // Now lets see if the and is true so we can go to the second part
          ctx.instruction_of_ctx.add(new LLVMJmpInstruction(var1, andFirstPartTrue, andFirstPartFalse));

          // Add the first part label
          ctx.instruction_of_ctx.add(new LLVMLabelInstruction(andFirstPartFalse));

          // Now lets see if the and is true so we can go to the second part
          ctx.instruction_of_ctx.add(new LLVMJmpInstruction(andEnd));

          // Add the second part label
          ctx.instruction_of_ctx.add(new LLVMLabelInstruction(andFirstPartTrue));

          p.expr_2.accept(new ExprVisitor(), ctx);
          Variable var2 = ctx.GetLastVariable();
          var2 = ctx.EnsureLoaded(var2);
          ctx.ClearLastVariable();

          // Lets write the and instruction of both var1 and var2
          Variable tmp = ctx.GetNewTempVairableWithTheSameTypeOf(var2);
          ctx.instruction_of_ctx.add(new LLVMAndInstruction(var1, var2, tmp));

          // Now jump to the end
          ctx.instruction_of_ctx.add(new LLVMJmpInstruction(andEnd));

          // Lets add the end label
          ctx.instruction_of_ctx.add(new LLVMLabelInstruction(andEnd));

          // Now lets decide the final result based on the branch we took using phi instruction
          Variable tmp1 = ctx.GetNewTempVairableWithTheSameTypeOf(var2);
          LLVMPhiInstruction llvmPhiInstruction = new LLVMPhiInstruction(tmp1);
          llvmPhiInstruction.addIncoming(tmp,  ctx.jmp_labels.isEmpty() ? andFirstPartTrue : ctx.jmp_labels.pop());
          llvmPhiInstruction.addIncoming(var1, andFirstPartFalse);
          ctx.instruction_of_ctx.add(llvmPhiInstruction);

          ctx.jmp_labels.push(andEnd);
          ctx.SetLastVariable(tmp1);
          return ctx;
        }

        public LLVMCodeGenCtx visit(jlc.lib.javalette.Absyn.EOr p, LLVMCodeGenCtx ctx) {
          if (IsNewExper) {
            // remove labels of last expressions
            IsNewExper = false;
            ctx.jmp_labels.clear();
          }

          // Before evaluating first part of or we should jump to a stupid label so that phi 
          // instruction can decide later based on it
          List<String> labels = ctx.GetNewLabelWithPrefix("or.first.true", "or.first.false", "or.end");
          String orFirstPartTrue = labels.get(0);
          String orFirstPartFalse = labels.get(1);
          String orEnd = labels.get(2);

          // Now we evaluate the first part of or
          p.expr_1.accept(new ExprVisitor(), ctx);
          Variable var1 = ctx.GetLastVariable();
          var1 = ctx.EnsureLoaded(var1);
          ctx.ClearLastVariable();

          // Now lets see if the or is false so we can go to the second part
          ctx.instruction_of_ctx.add(new LLVMJmpInstruction(var1, orFirstPartTrue, orFirstPartFalse));

          // Add the first part label
          ctx.instruction_of_ctx.add(new LLVMLabelInstruction(orFirstPartTrue));

          // Now lets see if the or is true so we can go to the second part
          ctx.instruction_of_ctx.add(new LLVMJmpInstruction(orEnd));

          // Add the second part label
          ctx.instruction_of_ctx.add(new LLVMLabelInstruction(orFirstPartFalse));

          p.expr_2.accept(new ExprVisitor(), ctx);
          Variable var2 = ctx.GetLastVariable();
          var2 = ctx.EnsureLoaded(var2);
          ctx.ClearLastVariable();

          // Lets write the or instruction of both var1 and var2
          Variable tmp = ctx.GetNewTempVairableWithTheSameTypeOf(var2);
          ctx.instruction_of_ctx.add(new LLVMOrInstruction(var1, var2, tmp));

          // Now jump to the end
          ctx.instruction_of_ctx.add(new LLVMJmpInstruction(orEnd));

          // Lets add the end label
          ctx.instruction_of_ctx.add(new LLVMLabelInstruction(orEnd));

          // Now lets decide the final result based on the branch we took using phi instruction
          Variable tmp1 = ctx.GetNewTempVairableWithTheSameTypeOf(var2);
          LLVMPhiInstruction llvmPhiInstruction = new LLVMPhiInstruction(tmp1);
          llvmPhiInstruction.addIncoming(tmp, ctx.jmp_labels.isEmpty() ? orFirstPartFalse : ctx.jmp_labels.pop());
          llvmPhiInstruction.addIncoming(var1, orFirstPartTrue);
          ctx.instruction_of_ctx.add(llvmPhiInstruction);

          ctx.jmp_labels.push(orEnd);
          ctx.SetLastVariable(tmp1);
          return ctx;
        }

        @Override
        public LLVMCodeGenCtx visit(ArrIndex p, LLVMCodeGenCtx ctx) {
            // Evaluate the array expression (should yield an i8* pointer)
            p.expr_1.accept(new ExprVisitor(), ctx);
            Variable arrPtr = ctx.GetLastVariable();
            arrPtr = ctx.EnsureLoaded(arrPtr);
            ctx.ClearLastVariable();

            // Evaluate the index expression
            p.expr_2.accept(new ExprVisitor(), ctx);
            Variable idxVar = ctx.GetLastVariable();
            idxVar = ctx.EnsureLoaded(idxVar);
            ctx.ClearLastVariable();

            //Determine the element type (from the ArrayVariable)
            VariableType elemType;
            if (arrPtr instanceof ArrayVariable) {
                elemType = ((ArrayVariable) arrPtr).GetArrayType().GetVariableType();
            } else {
                // Fallback or error
                throw new RuntimeException("ArrIndex: arrPtr is not an ArrayVariable");
            }

            //Bitcast the i8* to the correct element pointer type
            Variable elemPtrTypeVar;
            switch (elemType) {
                case Int:
                    elemPtrTypeVar = ctx.GetNewTempVairableWithTheSameTypeOf(new IntVariable(""));
                    ctx.instruction_of_ctx.add(
                        new LLVMBitcastInstruction(arrPtr, elemPtrTypeVar, "i32*")
                    );
                    break;
                case Double:
                    elemPtrTypeVar = ctx.GetNewTempVairableWithTheSameTypeOf(new DoubleVariable(""));
                    ctx.instruction_of_ctx.add(
                        new LLVMBitcastInstruction(arrPtr, elemPtrTypeVar, "double*")
                    );
                    break;
                case Boolean:
                    elemPtrTypeVar = ctx.GetNewTempVairableWithTheSameTypeOf(new BooleanVariable(""));
                    ctx.instruction_of_ctx.add(
                        new LLVMBitcastInstruction(arrPtr, elemPtrTypeVar, "i8*")
                    );
                    break;
                default:
                    throw new RuntimeException("ArrIndex: Unsupported element type");
            }

            //GEP to get the address of the element
            Variable gepResult = ctx.GetNewTempVairableWithTheSameTypeOf(elemPtrTypeVar);
            ctx.instruction_of_ctx.add(
                new LLVMGetElementPtrInstruction(elemPtrTypeVar, idxVar, gepResult, elemType)
            );

            //Load the value at that address
            Variable loaded = ctx.GetNewTempVairableWithTheSameTypeOf(
                elemType == VariableType.Int ? new IntVariable("") :
                elemType == VariableType.Double ? new DoubleVariable("") :
                new BooleanVariable("")
            );
            
            ctx.instruction_of_ctx.add(
                new jlc.main.Instructions.LLVM.LLVMLoadInstruction(gepResult, loaded)
            );

            // Set the result as the last variable
            ctx.SetLastVariable(loaded);
            return ctx;
        }

        @Override
        public LLVMCodeGenCtx visit(ArrLen p, LLVMCodeGenCtx ctx) {
        // Evaluate array expression
        p.expr_.accept(new ExprVisitor(), ctx);
        Variable arrPtr = ctx.GetLastVariable();
        arrPtr = ctx.EnsureLoaded(arrPtr);
        ctx.ClearLastVariable();

        // Call the runtime function to get the length
        Variable lenVar = ctx.GetNewTempVairableWithTheSameTypeOf(new IntVariable("last"));
        ctx.instruction_of_ctx.add(
            new LLVMFuncCallIntruction("array_length", lenVar, arrPtr)
        );

        ctx.SetLastVariable(lenVar);
        return ctx;
        
        }
        public LLVMCodeGenCtx visit(NewArr p, LLVMCodeGenCtx ctx) {
          // Evaluate the size expression
          p.expr_.accept(new ExprVisitor(), ctx);
          Variable sizeVar = ctx.GetLastVariable();
          sizeVar = ctx.EnsureLoaded(sizeVar);
          ctx.ClearLastVariable();

          // Always move the size into a temp register (even if it's a constant)
          Variable sizeTemp = ctx.GetNewTempVairableWithTheSameTypeOf(sizeVar);
          // ctx.instruction_of_ctx.add(new LLVMStoreInstruction(sizeVar, sizeTemp));
          Variable newVar = new IntVariable("0");
          newVar.SetVariableKind(VariableKind.ConstantVariable);
          ctx.instruction_of_ctx.add(
              new LLVMAddInstruction(AddType.Plus, newVar , sizeVar, sizeTemp)
          );
          // Check base type of array
          ctx = p.basetype_.accept(new BaseTypeVisitor(), ctx);
          Variable typeVar = ctx.GetLastVariable();
          VariableType arrType = typeVar.GetVariableType();
          ctx.ClearLastVariable();

          // Determine the allocation function
          String allocFunc;
          switch (arrType) {
              case Int:     allocFunc = "alloc_array_i32"; break;
              case Double:  allocFunc = "alloc_array_double"; break;
              case Boolean: allocFunc = "alloc_array_i8"; break;
              default: 
                  throw new RuntimeException("Unsupported array type: " + arrType);
          }

          // Allocate the array: call the runtime function, result is i8*
          Variable arrPtr = ctx.GetNewTempVairableWithTheSameTypeOf(new ArrayVariable("arr", arrType));
          ctx.instruction_of_ctx.add(new LLVMFuncCallIntruction(allocFunc, arrPtr, sizeTemp));
          
          // Set the result as the last variable (as i8* pointer)
          ctx.SetLastVariable(arrPtr);
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
