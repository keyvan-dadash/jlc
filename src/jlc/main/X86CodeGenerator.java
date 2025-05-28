package jlc.main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.CodeGenHelper;
import jlc.main.Instructions.x86.LinearScanAllocator;
import jlc.main.Instructions.x86.LivenessAnalysis;
import jlc.main.Instructions.x86.LinearScanAllocator.AllocationResult;
import jlc.main.Instructions.x86.LinearScanAllocator.AssignedInterval;
import jlc.main.Instructions.x86.IR.FuncDef;
import jlc.main.Instructions.x86.IR.IR;
import jlc.main.Variables.Variable;

/*
 * X86 code generator that constructs the whole pipline of:
 * 1. Translating AST to IR.
 * 2. Perform liveness analysis.
 * 3. Perform register allocation.
 * 4. Translate IR to x86 assembly codes.
 */
public class X86CodeGenerator {
    private final Ctx ctx;
    private final jlc.lib.javalette.Absyn.Prog parseTree;
    private final String outputFilename;

    public X86CodeGenerator(Ctx ctx,
                            jlc.lib.javalette.Absyn.Prog parseTree,
                            String outputFilename) {
        this.ctx            = ctx;
        this.parseTree      = parseTree;
        this.outputFilename = outputFilename;
    }

    public void GenerateCode(boolean dumpAsm,
                             boolean dumpIR,
                             boolean dumpAlloc) {
        IRCodeGenerator irGen = new IRCodeGenerator(ctx, parseTree);
        List<IR> irCodes = irGen.generateCode();

        if (dumpIR) printIR(irCodes);

        LivenessAnalysis la = new LivenessAnalysis();
        for (IR ir : irCodes) ir.PerformLivenessAnalysis(la);

        LinearScanAllocator allocator = new LinearScanAllocator();
        AllocationResult allocResult = allocator.allocate(la);

        if (dumpAlloc) printAllocationSorted(allocResult);

        CodeGenHelper helper =
            new CodeGenHelper(allocResult, allocator.getSpillSlots(), allocator.getSpillsByStep());

        for (IR ir : irCodes) {
            if (ir instanceof FuncDef) {
                ((FuncDef)ir).computeSpillVariables(
                    allocator.getSpillSlots(),
                    helper.getFuncFrames(),
                    helper.getFuncArgSize()
                );
            }
        }

        helper.getFuncArgSize().put("printString", 8);
        helper.getFuncArgSize().put("printInt",    8);
        helper.getFuncArgSize().put("printDouble", 8);
        helper.getFuncArgSize().put("readInt",     0);
        helper.getFuncArgSize().put("readDouble",  0);

        StringBuilder asm = new StringBuilder();
        for (IR ir : irCodes) {
            List<Instruction> insns = ir.GenerateX86Code(helper);
            for (Instruction ins : insns) {
                asm.append(ins.GenerateInstruction()).append("\n");
            }
        }

        try (BufferedWriter w = new BufferedWriter(new FileWriter(outputFilename))) {
            w.write(asm.toString());
        } catch (IOException e) {
            System.err.println("Error writing x86 to " + outputFilename + ": " + e.getMessage());
        }

        if (dumpAsm) System.out.println(asm.toString());
    }

    private void printIR(List<IR> irCodes) {
        System.out.println("---- DUMP IR ----");
        for (IR inst : irCodes) {
            System.out.println(inst.GetIR());
        }
        System.out.println("-----------------");
    }

    private void printAllocationSorted(AllocationResult allocationResult) {
        System.out.println("---- DUMP REG ALLOCATION ----");
        for (Map.Entry<Variable, List<AssignedInterval>> e 
                : allocationResult.map.entrySet()) {
            Variable var = e.getKey();
            List<AssignedInterval> pieces = new ArrayList<>(e.getValue());
            pieces.sort(Comparator.comparingInt(p -> p.start));
            System.out.printf("Variable %s:\n", var.GetVariableName());
            for (AssignedInterval piece : pieces) {
                String where = (piece.reg != null)
                    ? piece.reg.getName()
                    : "<spilled>";
                System.out.printf("  Live [%d..%d) → %s\n",
                    piece.start, piece.end, where);
            }
        }
        System.out.println("-----------------------------");
    }
}
