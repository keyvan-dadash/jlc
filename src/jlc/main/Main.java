package jlc.main;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.CodeGenHelper;
import jlc.main.Instructions.x86.LinearScanAllocator;
import jlc.main.Instructions.x86.LivenessAnalysis;
import jlc.main.Instructions.x86.IR.FuncDef;
import jlc.main.Instructions.x86.IR.IR;
import jlc.main.Instructions.x86.LinearScanAllocator.AllocationResult;
import jlc.main.Instructions.x86.LinearScanAllocator.AssignedInterval;
import jlc.main.Variables.Variable;

public class Main {

    public static void debugPrintAllocationSorted(AllocationResult allocationResult) {
        for (Map.Entry<Variable, List<AssignedInterval>> e 
                : allocationResult.map.entrySet()) {
            Variable var = e.getKey();
            List<AssignedInterval> pieces = new ArrayList<>(e.getValue());
            // Sort pieces by start
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
    }

    public static void main(String args[]) {
        try {
            TypeChecker typeChecker = new TypeChecker();
            typeChecker.performTypeCheckOnFile(System.in);

            // CodeGenerator codeGenerator = new CodeGenerator(
            //     typeChecker.GetCtx(),
            //     typeChecker.GetTree(),
            //     "output.ll");
            // codeGenerator.GenerateCode(true);
            
            X86CodeGenerator x86gen = new X86CodeGenerator(typeChecker.GetCtx(),
                                    typeChecker.parse_tree,
                                    "output.asm");
            x86gen.GenerateCode(true, false, false);
        } catch(Exception e) {
            if (args.length > 0 && args[0].equals(new String("v"))) {
                System.err.println(String.format("failed to compile the given input:\n%s", e.getMessage()));
                e.printStackTrace();
            }
            System.err.println("ERROR");
            System.exit(1);
        } catch (Error e) {
            System.err.println("ERROR");
            System.exit(1);
        }

        System.err.println("OK");
    }
}
