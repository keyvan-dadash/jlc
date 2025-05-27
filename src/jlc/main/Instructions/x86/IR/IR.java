package jlc.main.Instructions.x86.IR;

import java.util.List;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.CodeGenHelper;
import jlc.main.Instructions.x86.LivenessAnalysis;

public interface IR {
    String GetIR();
    void PerformLivenessAnalysis(LivenessAnalysis livenessAnalysis);
    List<Instruction> GenerateX86Code(CodeGenHelper codeGenHelper);
}
