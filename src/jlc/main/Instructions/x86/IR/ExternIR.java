package jlc.main.Instructions.x86.IR;

import java.util.Collections;
import java.util.List;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.CodeGenHelper;
import jlc.main.Instructions.x86.LivenessAnalysis;
import jlc.main.Instructions.x86.Instructions.X86ExternInstruction;

/**
 * Emit a “.extern <symbol>” directive.
 */
public class ExternIR implements IR {
    private final String symbol;

    public ExternIR(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String GetIR() {
        return String.format("EXTERN %s", symbol);
    }

    @Override
    public void PerformLivenessAnalysis(LivenessAnalysis livenessAnalysis) {
        // no-op
    }

    @Override
    public List<Instruction> GenerateX86Code(CodeGenHelper helper) {
        return Collections.<Instruction>singletonList(
            new X86ExternInstruction(symbol)
        );
    }
}