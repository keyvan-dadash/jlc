package jlc.main.Instructions.x86;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Instructions.X86AddImmediateInstruction;
import jlc.main.Instructions.x86.Instructions.X86DirectiveInstruction;
import jlc.main.Instructions.x86.Instructions.X86MoveFPInstruction;
import jlc.main.Instructions.x86.Instructions.X86PopInstruction;
import jlc.main.Instructions.x86.Instructions.X86PushInstruction;
import jlc.main.Instructions.x86.Instructions.X86SubImmediateInstruction;

public final class RegisterSaver {
    private RegisterSaver() {}

    public static void save(
            CodeGenHelper helper,
            List<Instruction> out,
            Register[] candidates,
            Set<Register> toSave) {

        List<Register> regs = new ArrayList<>();
        for (Register r : candidates) {
            if (toSave.contains(r)) regs.add(r);
        }

        if (regs.size() == 0) return;

        X86DirectiveInstruction x86DirectiveInstruction = new X86DirectiveInstruction("; saving registers");
        x86DirectiveInstruction.AddNumOfSpaceForPrefix(4);
        out.add(x86DirectiveInstruction);

        regs.sort(Comparator.comparingInt(Enum::ordinal));

        for (Register r : regs) {
            if (r.name().startsWith("XMM")) {
                X86SubImmediateInstruction sub =
                    new X86SubImmediateInstruction(
                      Operand.of(Register.RSP), 8
                    );
                sub.AddNumOfSpaceForPrefix(4);
                out.add(sub);
                helper.addToCurrentFuncStackSize(8);
    
                // movss [rsp], xmmN
                X86MoveFPInstruction st = new X86MoveFPInstruction(
                    true,
                    Operand.of(new Address(Register.RSP, 0)),
                    Operand.of(r)
                );
                st.AddNumOfSpaceForPrefix(4);
                out.add(st);
    
            } else {
                // normal GP reg push
                X86PushInstruction push = new X86PushInstruction(Operand.of(r));
                push.AddNumOfSpaceForPrefix(4);
                out.add(push);
                helper.addToCurrentFuncStackSize(8);
            }
        }

        X86DirectiveInstruction x86DirectiveInstruction1 = new X86DirectiveInstruction("; end of saving registers");
        x86DirectiveInstruction1.AddNumOfSpaceForPrefix(4);
        out.add(x86DirectiveInstruction1);
    }

    public static void restore(
            CodeGenHelper helper,
            List<Instruction> out,
            Register[] candidates,
            Set<Register> toSave) {

        List<Register> regs = new ArrayList<>();
        for (Register r : candidates) {
            if (toSave.contains(r)) regs.add(r);
        }

        if (regs.size() == 0) return;

        X86DirectiveInstruction x86DirectiveInstruction = new X86DirectiveInstruction("; restoring registers");
        x86DirectiveInstruction.AddNumOfSpaceForPrefix(4);
        out.add(x86DirectiveInstruction);

        regs.sort((r1, r2) -> Integer.compare(r2.ordinal(), r1.ordinal()));

        for (Register r : regs) {
            if (r.name().startsWith("XMM")) {
                // movss xmmN, [rsp]
                X86MoveFPInstruction ld = new X86MoveFPInstruction(
                    /* isDouble= */ true,
                    Operand.of(r),
                    Operand.of(new Address(Register.RSP, 0))
                );
                ld.AddNumOfSpaceForPrefix(4);
                out.add(ld);
    
                // add rsp,8
                X86AddImmediateInstruction add =
                    new X86AddImmediateInstruction(
                      Operand.of(Register.RSP), 8
                    );
                add.AddNumOfSpaceForPrefix(4);
                out.add(add);
                helper.addToCurrentFuncStackSize(-8);
    
            } else {
                // normal GP reg pop
                X86PopInstruction pop = new X86PopInstruction(Operand.of(r));
                pop.AddNumOfSpaceForPrefix(4);
                out.add(pop);
                helper.addToCurrentFuncStackSize(-8);
            }
        }

        X86DirectiveInstruction x86DirectiveInstruction1 = new X86DirectiveInstruction("; end of restoring registers");
        x86DirectiveInstruction1.AddNumOfSpaceForPrefix(4);
        out.add(x86DirectiveInstruction1);
    }
}