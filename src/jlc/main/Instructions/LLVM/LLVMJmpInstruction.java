package jlc.main.Instructions.LLVM;

import jlc.main.Instructions.Instruction;
import jlc.main.Variables.Variable;

public class LLVMJmpInstruction implements Instruction {
    private int numOfSpace;
    Variable cond;
    String label1;
    String label2;
    Boolean isConditionJMP;

    LLVMJmpInstruction() {

    }

    public LLVMJmpInstruction(Variable cond, String label1, String label2) {
        this.SetVariables(cond, label1, label2);
    }

    public LLVMJmpInstruction(String label1) {
        this.SetVariables(label1);
    }

    public void SetVariables(Variable cond, String label1, String label2) {
        this.cond = cond;
        this.label1 = label1;
        this.label2 = label2;
        isConditionJMP = true;
    }

    public void SetVariables(String label1) {
        this.label1 = label1;
        isConditionJMP = false;
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        if (!isConditionJMP) {
            return String.format("%sbr label %%%s", 
                    Utils.GetNumOfSpace(numOfSpace),
                    label1);
        }

        return String.format("%sbr i1 %s, label %%%s, label %%%s", 
                Utils.GetNumOfSpace(numOfSpace),
                Utils.VariableToLLVMVariable(cond),
                label1,
                label2);
    }
}
