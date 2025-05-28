package jlc.main.Instructions.x86.IR;

import java.util.Collections;
import java.util.List;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.CodeGenHelper;
import jlc.main.Instructions.x86.LivenessAnalysis;
import jlc.main.Instructions.x86.Instructions.X86GlobalInstruction;

/**
 * Outputs a “.globl <symbol>” directive.
 */
public class GlobalIR implements IR {
    private final String symbol;

    public GlobalIR(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String GetIR() {
        return String.format("GLOBAL %s", symbol);
    }

    @Override
    public void PerformLivenessAnalysis(LivenessAnalysis livenessAnalysis) {
        return;
    }

    @Override
    public List<Instruction> GenerateX86Code(CodeGenHelper helper) {
        return Collections.<Instruction>singletonList(
            new X86GlobalInstruction(symbol)
        );
    }
}