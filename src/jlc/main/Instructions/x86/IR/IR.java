package jlc.main.Instructions.x86.IR;

import java.util.List;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.CodeGenHelper;
import jlc.main.Instructions.x86.LivenessAnalysis;

// IR interface is an common interface that should be implemented
// by any intermediate representation in order to generate IR and 
// later tranforms to x86 assembly.
public interface IR {
    // GetIR returns the string representation of IR
    String GetIR();

    // PerformLivenessAnalysis extracts each variables liveness in order to 
    // do register allocation later.
    void PerformLivenessAnalysis(LivenessAnalysis livenessAnalysis);

    // GenerateX86Code transforms this IR to x86 assembly code
    List<Instruction> GenerateX86Code(CodeGenHelper codeGenHelper);
}
