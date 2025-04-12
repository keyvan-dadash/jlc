package jlc.main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import jlc.main.Instructions.Instruction;

public class CodeGenerator {
    private Ctx ctx;
    private String outputFilename;
    jlc.lib.javalette.Absyn.Prog parse_tree;

    public CodeGenerator(Ctx ctx, jlc.lib.javalette.Absyn.Prog parse_tree, String outputFilename) {
        this.ctx = ctx;
        this.outputFilename = outputFilename;
        this.parse_tree = parse_tree;
    }

    public void GenerateCode(boolean shouldWriteToOut) {
        LLVMCodeGenCtx llvmCodeGenCtx = new LLVMCodeGenCtx();
        llvmCodeGenCtx.functions = ctx.functions;

        LLVMCodeGeneratorVisit skel_type = new LLVMCodeGeneratorVisit();
        LLVMCodeGeneratorVisit.ProgVisitor codeGenVisitor = skel_type.new ProgVisitor();
        codeGenVisitor.SetLLVMCodeGenCtx(llvmCodeGenCtx);
        jlc.lib.javalette.Absyn.Prog.Visitor<LLVMCodeGenCtx, LLVMCodeGenCtx> visitor = codeGenVisitor;

        // Run the visitor on the parse tree.
        parse_tree.accept(visitor, null);

        // Build the final LLVM IR code by iterating through the global and context instructions.
        StringBuilder code = new StringBuilder();

        code.append("declare void @printInt(i32)\n");
        code.append("declare void @printDouble(double)\n");
        code.append("declare void @printString(i8*)\n");
        code.append("declare i32 @readInt()\n");
        code.append("declare double @readDouble()\n\n");

        // Append global instructions (if any).
        for (Instruction instr : llvmCodeGenCtx.global_instructions) {
            code.append(instr.GenerateInstruction()).append("\n");
        }
        // Append instructions specific to the current context.
        for (Instruction instr : llvmCodeGenCtx.instruction_of_ctx) {
            code.append(instr.GenerateInstruction()).append("\n");
        }

        // Write the generated code to the output file.
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilename))) {
            writer.write(code.toString());
        } catch (IOException e) {
            System.err.println("Error writing to file " + outputFilename + ": " + e.getMessage());
        }

        if (shouldWriteToOut) {
            System.out.println(code.toString());
        }
    }

}
