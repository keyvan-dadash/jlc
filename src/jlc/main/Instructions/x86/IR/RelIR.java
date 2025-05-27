package jlc.main.Instructions.x86.IR;

import java.util.ArrayList;
import java.util.List;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.CodeGenHelper;
import jlc.main.Instructions.x86.LivenessAnalysis;
import jlc.main.Instructions.x86.Operand;
import jlc.main.Instructions.x86.Register;
import jlc.main.Instructions.x86.Utils;
import jlc.main.Instructions.x86.Instructions.X86CmpFPInstruction;
import jlc.main.Instructions.x86.Instructions.X86CmpInstruction;
import jlc.main.Instructions.x86.Instructions.X86MovzxInstruction;
import jlc.main.Instructions.x86.Instructions.X86SetccInstruction;
import jlc.main.Operations.RelType;
import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableType;

/**
 * Represents a three‐operand relational operation in the x86 IR:
 *   dest = (src1 rel src2)
 * where rel ∈ {LTH, LE, GTH, GE, EQU, NE}.
 *
 * Lowering typically becomes:
 *   cmp src1, src2
 *   set<cond> dest8
 *   movzx dest, dest8
 *
 * To support SETcc, we fix dest to EAX (using AL for the byte write).
 */
public class RelIR implements IR {
    private final RelType type;
    private Variable dest;
    private Variable src1;
    private Variable src2;

    public RelIR(RelType type) {
        this.type = type;
    }

    public RelIR(RelType type, Variable dest, Variable src1, Variable src2) {
        this.type = type;
        setOperands(dest, src1, src2);
    }

    public void setOperands(Variable dest, Variable src1, Variable src2) {
        this.dest = dest;
        this.src1 = src1;
        this.src2 = src2;
    }

    @Override
    public String GetIR() {
        if (dest == null || src1 == null || src2 == null) {
            throw new IllegalStateException("RelIR operands not fully initialized");
        }
        String opcode;
        switch (type) {
            case LTH: opcode = "LTH"; break;
            case LE:  opcode = "LE";  break;
            case GTH: opcode = "GTH"; break;
            case GE:  opcode = "GE";  break;
            case EQU: opcode = "EQU"; break;
            case NE:  opcode = "NE";  break;
            default:
                throw new IllegalArgumentException("Unsupported RelType: " + type);
        }
        return String.format("%s %s, %s, %s",
                opcode,
                dest.GetVariableName(),
                src1.GetVariableName(),
                src2.GetVariableName());
    }

    @Override
    public void PerformLivenessAnalysis(LivenessAnalysis la) {
        // record uses of src1/src2
        if (Utils.isVirtualVariable(src1)) la.recordVar(src1);
        if (Utils.isVirtualVariable(src2)) la.recordVar(src2);
        // record definition of dest
        if (Utils.isVirtualVariable(dest)) {
            la.recordVar(dest);
            // pin dest to RAX so SETcc writes into AL and we zero-extend from there
            la.setFixedRegister(dest, Register.RAX);
        }
        la.finishStep();
    }

    @Override
    public List<Instruction> GenerateX86Code(CodeGenHelper codeGenHelper) {
        List<Instruction> out = new ArrayList<>();

        codeGenHelper.spillCurrentStep(out);

        // 1) Materialize src1/src2 into registers or scratch if spilled
        Operand op1 = codeGenHelper.ensureInRegister(src1, out);
        Operand op2 = codeGenHelper.ensureInRegister(src2, out);

        // 2) cmp vs. ucomiss depending on type
        if (src1.GetVariableType() == VariableType.Double) {
            // 32-bit “double” → unordered compare
            X86CmpFPInstruction cmpfp = new X86CmpFPInstruction(
                /* isDouble = */ true,
                op1, op2
            );
            cmpfp.AddNumOfSpaceForPrefix(4);
            out.add(cmpfp);
        } else {
            X86CmpInstruction cmpi = new X86CmpInstruction(op1, op2);
            cmpi.AddNumOfSpaceForPrefix(4);
            out.add(cmpi);
        }

        // 3) SETcc into AL (low byte of RAX)
        Operand al = Operand.of(Register.AL);
        X86SetccInstruction setcc = new X86SetccInstruction(type, al);
        setcc.AddNumOfSpaceForPrefix(4);
        out.add(setcc);

        // 4) Zero-extend AL into RAX via MOVZX
        Operand rax = Operand.of(Register.RAX);
        X86MovzxInstruction movzx = new X86MovzxInstruction(rax, al);
        movzx.AddNumOfSpaceForPrefix(4);
        out.add(movzx);

        // 5) Advance the IR step
        codeGenHelper.finishStep();
        return out;
    }
}
