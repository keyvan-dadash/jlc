package jlc.main.Instructions.x86.IR;

import java.util.Collections;
import java.util.List;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.CodeGenHelper;
import jlc.main.Instructions.x86.LivenessAnalysis;
import jlc.main.Instructions.x86.Instructions.X86SectionInstruction;

/**
 * Sets current section. For example:
 *   SECTION .text
 *   SECTION .data
 *   SECTION .rodata
 */
public class SectionIR implements IR {
    private final String sectionName;

    public SectionIR(String sectionName) {
        this.sectionName = sectionName;
    }

    @Override
    public String GetIR() {
        return String.format("SECTION %s", sectionName);
    }

    @Override
    public void PerformLivenessAnalysis(LivenessAnalysis livenessAnalysis) {
        return;
    }

    @Override
    public List<Instruction> GenerateX86Code(CodeGenHelper helper) {
        return Collections.<Instruction>singletonList(
            new X86SectionInstruction(sectionName)
        );
    }
}