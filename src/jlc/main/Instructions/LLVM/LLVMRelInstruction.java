package jlc.main.Instructions.LLVM;

import jlc.main.Instructions.Instruction;
import jlc.main.Operations.RelType;
import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableType;

public class LLVMRelInstruction implements Instruction {
    
    private RelType relType;
    private Variable var1;
    private Variable var2;
    private Variable result;
    private int numOfSpace;

    public LLVMRelInstruction(RelType relType) {
        this.relType = relType;
    }
    
    public LLVMRelInstruction(RelType relType, Variable var1, Variable var2, Variable result) {
        this.relType = relType;
        setVariables(var1, var2, result);
    }
    
    public void setVariables(Variable var1, Variable var2, Variable result) {
        this.var1 = var1;
        this.var2 = var2;
        this.result = result;
    }
    
    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }
    
    @Override
    public String GenerateInstruction() {
        if (var1 == null || var2 == null || result == null) {
            throw new RuntimeException("Variables not set for LLVMRelInstruction");
        }

        VariableType vt = var1.GetVariableType();
        switch(vt) {
            case Int:
                return generateRelForInt();
            case Double:
                return generateRelForDouble();
            case Boolean:
                return generateRelForBoolean();
            default:
                throw new RuntimeException("Unsupported variable type for relational instruction: " + vt);
        }
    }
    
    private String generateRelForInt() {
        String predicate;
        switch(relType) {
            case LTH: predicate = "slt"; break;
            case LE:  predicate = "sle"; break;
            case GTH: predicate = "sgt"; break;
            case GE:  predicate = "sge"; break;
            case EQU: predicate = "eq";  break;
            case NE:  predicate = "ne";  break;
            default: throw new RuntimeException("Unknown relational type for int");
        }
        
        return String.format("%s%s = icmp %s i32 %s, %s",
                Utils.GetNumOfSpace(this.numOfSpace),
                Utils.VariableToLLVMVariable(result),
                predicate,
                Utils.VariableToLLVMVariable(var1),
                Utils.VariableToLLVMVariable(var2));
    }
    
    private String generateRelForDouble() {
        String predicate;
        switch(relType) {
            case LTH: predicate = "olt"; break;
            case LE:  predicate = "ole"; break;
            case GTH: predicate = "ogt"; break;
            case GE:  predicate = "oge"; break;
            case EQU: predicate = "oeq"; break;
            case NE:  predicate = "one"; break;
            default: throw new RuntimeException("Unknown relational type for double");
        }
        return String.format("%s%s = fcmp %s double %s, %s",
                Utils.GetNumOfSpace(this.numOfSpace),
                Utils.VariableToLLVMVariable(result),
                predicate,
                Utils.VariableToLLVMVariable(var1),
                Utils.VariableToLLVMVariable(var2));
    }
    
    private String generateRelForBoolean() {
        if (relType != RelType.EQU && relType != RelType.NE) {
            throw new RuntimeException("Boolean operands only support EQU and NE relational operations");
        }
        String predicate = (relType == RelType.EQU) ? "eq" : "ne";
        return String.format("%s%s = icmp %s i1 %s, %s",
                Utils.GetNumOfSpace(this.numOfSpace),
                Utils.VariableToLLVMVariable(result),
                predicate,
                Utils.VariableToLLVMVariable(var1),
                Utils.VariableToLLVMVariable(var2));
    }
}
