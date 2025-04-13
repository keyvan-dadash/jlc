package jlc.main.Instructions.LLVM;

import jlc.main.Instructions.Instruction;
import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableType;
import java.util.ArrayList;
import java.util.List;

public class LLVMPhiInstruction implements Instruction {

    private Variable result;
    private VariableType type;
    private List<PhiOperand> incoming;
    private int numOfSpace;

    public LLVMPhiInstruction(Variable result) {
        this.result = result;
        this.type = result.GetVariableType();
        this.incoming = new ArrayList<>();
    }

    public void addIncoming(Variable value, String predLabel) {
        incoming.add(new PhiOperand(value, predLabel));
    }

    @Override
    public void AddNumOfSpaceForPrefix(int num) {
        this.numOfSpace = num;
    }

    @Override
    public String GenerateInstruction() {
        StringBuilder sb = new StringBuilder();
        sb.append(Utils.GetNumOfSpace(this.numOfSpace));
        sb.append(Utils.VariableToLLVMVariable(result));
        sb.append(" = phi ");
        sb.append(Utils.VariableTypeToLLVMVariableType(type));
        sb.append(" ");
        for (int i = 0; i < incoming.size(); i++) {
            PhiOperand op = incoming.get(i);
            sb.append("[ ");
            sb.append(Utils.VariableToLLVMVariable(op.value));
            sb.append(", %");
            sb.append(op.label);
            sb.append(" ]");
            if (i != incoming.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private static class PhiOperand {
        Variable value;
        String label;
        PhiOperand(Variable value, String label) {
            this.value = value;
            this.label = label;
        }
    }
}
