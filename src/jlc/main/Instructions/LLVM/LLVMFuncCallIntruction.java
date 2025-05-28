package jlc.main.Instructions.LLVM;

import java.util.ArrayList;
import java.util.List;

import jlc.main.Function;
import jlc.main.Instructions.Instruction;
import jlc.main.Variables.Variable;
import jlc.main.Variables.VoidVariable;

public class LLVMFuncCallIntruction implements Instruction {
    private Variable return_var;
    private List<Variable> args;
    private Function fn;
    private String globalFn;
    private int numOfSpace;

    public LLVMFuncCallIntruction() {
        this.args = new ArrayList<>();
    }

    public LLVMFuncCallIntruction(Function fn, Variable returnVar, List<Variable> args) {
        this.args = new ArrayList<>();
        this.SetVariables(fn, returnVar, args);
    }
    
    public LLVMFuncCallIntruction(String fnName, Variable returnVar, Variable... args) {
        this.globalFn = fnName;
        this.return_var = returnVar;
        this.fn = null;
        this.args = new ArrayList<>();
        for (Variable v : args) this.args.add(v);
        AddNumOfSpaceForPrefix(4);
    }


    public void SetVariables(Function fn, Variable returnVar, List<Variable> args) {
        this.return_var = returnVar;
        this.args = args;
        this.fn = fn;
        AddNumOfSpaceForPrefix(4);
    }


    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        String fnName = (fn != null) ? fn.fn_name : globalFn;
        if(return_var.IsSameAs(new VoidVariable(""))){
            // Return of the function is void, so we dont need to assign it to anything.
            return String.format("%scall void @%s(%s)",
                Utils.GetNumOfSpace(this.numOfSpace),
                fnName,
                generateArgsStr());
        }
        return String.format("%s%s = call %s @%s(%s)",
            Utils.GetNumOfSpace(this.numOfSpace),
            Utils.VariableToLLVMVariable(return_var),
            Utils.VariableTypeToLLVMVariableType(return_var.GetVariableType()),
            fnName,
            generateArgsStr());
}

    private String generateArgsStr() {
        String argsStr = "";
        int index = 0;
        for (Variable arg : args) {
            if (index != 0)
                argsStr = argsStr + ", ";
            argsStr = argsStr + Utils.VariableTypeToLLVMVariableType(arg.GetVariableType()) + " " + Utils.VariableToLLVMVariable(arg);
            index++;
        }

        return argsStr;
    }
}
