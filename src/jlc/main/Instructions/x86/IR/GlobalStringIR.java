package jlc.main.Instructions.x86.IR;

import java.util.ArrayList;
import java.util.List;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.CodeGenHelper;
import jlc.main.Instructions.x86.LivenessAnalysis;
import jlc.main.Instructions.x86.Instructions.X86DirectiveInstruction;

/**
 * Represents a global string in the x86 IR.
 *  
**/
public class GlobalStringIR implements IR {
    private final String label;
    private final String value;

    public GlobalStringIR(String label, String value) {
        this.label = label;
        this.value = value;
    }

    @Override
    public String GetIR() {
        return String.format("GLOBALSTR %s, \"%s\"", 
            label, 
            escapeString(value));
    }

    private String escapeString(String s) {
        return s
            .replace("\\", "\\\\")
            .replace("\"", "\\\"");
    }

    @Override
    public void PerformLivenessAnalysis(LivenessAnalysis livenessAnalysis) {
        return;
    }

    @Override
    public List<Instruction> GenerateX86Code(CodeGenHelper helper) {
        List<Instruction> out = new ArrayList<>();
        out.add(new X86DirectiveInstruction(label + ":"));
        out.add(new X86DirectiveInstruction(
            String.format("    db \"%s\", 0", escapeString(value))
        ));
        return out;
    }
}
