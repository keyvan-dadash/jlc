package jlc.main;

import jlc.lib.javalette.Absyn.Prog;
import jlc.main.Instructions.x86.IR.ExternIR;
import jlc.main.Instructions.x86.IR.GlobalIR;
import jlc.main.Instructions.x86.IR.IR;
import jlc.main.Instructions.x86.IR.SectionIR;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates your x86‐style IR by running an IR‐focused visitor over the parse tree,
 * then returning the combined global‐scope and function‐scope IR instructions.
 */
public class IRCodeGenerator {
    private final Ctx ctx;
    private final Prog parseTree;

    public IRCodeGenerator(Ctx ctx, Prog parseTree) {
        this.ctx = ctx;
        this.parseTree = parseTree;
    }

    /**
     * Run the IR generator and return a list of IR instructions.
     * Global instructions come first, followed by the function‐local instructions.
     */
    public List<IR> generateCode() {
        // 1) Create an IR code‐gen context
        X86CodeGenCtx irCtx = new X86CodeGenCtx();
        irCtx.functions = ctx.functions;

        // 2) Run the IR visitor on the parse tree
        X86IRGeneratorVisit irVisit = new X86IRGeneratorVisit();
        X86IRGeneratorVisit.ProgVisitor progVisitor = irVisit.new ProgVisitor();
        progVisitor.SetX86CodeGenCtx(irCtx);
        parseTree.accept(progVisitor, null);

        // 3) Combine global and local instructions into a single list
        List<IR> combined = new ArrayList<>();
        // 1) rodata section
        combined.add(new SectionIR(".rodata"));
        //    *your* user‐defined globals (strings, doubles, etc.)
        combined.addAll(irCtx.global_instructions);

        // 2) text section + externs + global main
        combined.add(new SectionIR(".text"));
        combined.add(new ExternIR("printString"));
        combined.add(new ExternIR("printInt"));
        combined.add(new ExternIR("printDouble"));
        combined.add(new ExternIR("readInt"));
        combined.add(new ExternIR("readDouble"));
        combined.add(new GlobalIR("main"));

        combined.addAll(irCtx.instruction_of_ctx);
        return combined;
    }
}
