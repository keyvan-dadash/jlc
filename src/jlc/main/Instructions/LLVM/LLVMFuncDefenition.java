package jlc.main.Instructions.LLVM;

import jlc.main.Function;
import jlc.main.Instructions.Instruction;
import jlc.main.Variables.Variable;
import jlc.main.Variables.VoidVariable;

public class LLVMFuncDefenition implements Instruction {
    private Function fn;
    private int numOfSpace;

    public LLVMFuncDefenition() {
        
    }

    public LLVMFuncDefenition(Function fn) {
        this.SetVariables(fn);
    }

    public void SetVariables(Function fn) {
        this.fn = fn;
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        if (fn.return_var.IsSameAs(new VoidVariable(""))) {
            return String.format("%sdefine void @%s(%s) {\nentry:", 
                Utils.GetNumOfSpace(this.numOfSpace),
                fn.fn_name,
                generateArgsStr());
        }

        return String.format("%sdefine %s @%s(%s) {\nentry:", 
                Utils.GetNumOfSpace(this.numOfSpace),
                Utils.VariableTypeToLLVMVariableType(fn.return_var.GetVariableType()),
                fn.fn_name,
                generateArgsStr());
    }

    private String generateArgsStr() {
        String argsStr = "";
        int index = 0;
        for (Variable arg : fn.func_args) {
            if (index != 0)
                argsStr = argsStr + ", ";
            argsStr = argsStr + Utils.VariableTypeToLLVMVariableType(arg.GetVariableType()) + " " + Utils.VariableToLLVMVariable(arg);
        }

        return argsStr;
    }
}
