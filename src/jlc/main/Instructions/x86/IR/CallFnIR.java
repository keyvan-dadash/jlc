package jlc.main.Instructions.x86.IR;

import jlc.main.Function;
import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableType;
import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Address;
import jlc.main.Instructions.x86.CodeGenHelper;
import jlc.main.Instructions.x86.LivenessAnalysis;
import jlc.main.Instructions.x86.Operand;
import jlc.main.Instructions.x86.Register;
import jlc.main.Instructions.x86.RegisterSaver;
import jlc.main.Instructions.x86.Utils;
import jlc.main.Instructions.x86.Instructions.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CallFnIR implements IR {
    private final Function function;
    private final List<Variable> args;
    private final Variable result;

    public CallFnIR(Function function, List<Variable> args) {
        this(function, args, null);
    }

    public CallFnIR(Function function, List<Variable> args, Variable result) {
        this.function = function;
        this.args     = args;
        this.result   = result;
    }

    @Override
    public String GetIR() {
        String argList = args.stream()
            .map(Variable::GetVariableName)
            .collect(Collectors.joining(", "));
        if (result != null) {
            return String.format("CALL %s, %s%s%s",
                result.GetVariableName(),
                function.fn_name,
                argList.isEmpty() ? "" : ", ",
                argList);
        } else {
            return String.format("CALL %s%s%s",
                function.fn_name,
                argList.isEmpty() ? "" : ", ",
                argList);
        }
    }

    @Override
    public void PerformLivenessAnalysis(LivenessAnalysis la) {
        // record argument uses
        for (Variable v : args) {
            if (Utils.isVirtualVariable(v)) {
                la.recordVar(v);
            }
        }
        // record result definition + fixed reg
        if (result != null && Utils.isVirtualVariable(result)) {
            la.recordVar(result);
        }
        la.finishStep();
    }

    @Override
    public List<Instruction> GenerateX86Code(CodeGenHelper helper) {
        List<Instruction> out = new ArrayList<>();

        helper.spillCurrentStep(out);

        int totalBytes = 0;

        // Lets save registers
        RegisterSaver.save(helper, out, Register.callerSave(), helper.getLiveRegistersAtCurrentStep());

        // For padding and set later the padding size
        X86SubImmediateInstruction subAlign = new X86SubImmediateInstruction(Operand.of(Register.RSP), 0);
        subAlign.AddNumOfSpaceForPrefix(4);
        out.add(subAlign);

        // 1) Push / reserve each arg right-to-left, in original order
        for (int i = args.size() - 1; i >= 0; --i) {
            Variable v = args.get(i);
            // System.out.printf("Running push for func %s Arg: %s\n", helper.getCurrentFunc(), v.GetVariableName());
            VariableType vt = v.GetVariableType();

            if (Utils.isGlobalVariable(v) && v.GetVariableType() == VariableType.String) {
                Register tmp = Register.gpScratch();
                X86LeaInstruction lea = new X86LeaInstruction(
                    Operand.of(tmp),
                    Operand.of(new Address(Register.RIP, v.GetVariableName(), Utils.memSizeForVariable(v)))
                );
                
                lea.AddNumOfSpaceForPrefix(4);
                out.add(lea);
                Operand op = Operand.of(tmp);
                X86PushInstruction push = new X86PushInstruction(op);
                push.AddNumOfSpaceForPrefix(4);
                out.add(push);
                helper.addToCurrentFuncStackSize(8);
                totalBytes += 8;
                continue;
            }

            if (vt == VariableType.Double) {
                // Reserve 8 bytes for this FP argument
                X86SubImmediateInstruction sub =
                    new X86SubImmediateInstruction(
                        Operand.of(Register.RSP),
                        8
                    );
                sub.AddNumOfSpaceForPrefix(4);
                out.add(sub);
                helper.addToCurrentFuncStackSize(8);
                totalBytes += 8;

                // Move the double into [rsp]
                Operand src = helper.ensureInRegister(v, out);
                Address addr = new Address(Register.RSP, 0);
                X86MoveFPInstruction st = new X86MoveFPInstruction(
                    /*isDouble=*/true,
                    Operand.of(addr),
                    src
                );
                st.AddNumOfSpaceForPrefix(4);
                out.add(st);

            } else {
                // A normal 8-byte push for ints, booleans, pointers, etc.
                Operand op = helper.ensureInRegister(v, out);
                X86PushInstruction push = new X86PushInstruction(op);
                push.AddNumOfSpaceForPrefix(4);
                out.add(push);
                helper.addToCurrentFuncStackSize(8);
                totalBytes += 8;
            }
        }

        // 2) Align to 16 bytes if needed
        int curSize = helper.getCurrentFuncStackSize();
        int aligned = Utils.alignTo16(curSize);
        // System.out.printf("%s %s %s\n", helper.getCurrentFunc(), aligned, curSize);
        int pad = aligned - curSize;
        if (pad > 0) {
            subAlign.setOperands(Operand.of(Register.RSP), pad);
            helper.addToCurrentFuncStackSize(pad);
            totalBytes += pad;
        }

        // 3) The CALL itself
        X86CallInstruction call = new X86CallInstruction(function.fn_name);
        call.AddNumOfSpaceForPrefix(4);
        out.add(call);

        // 4) Clean up caller stack in one go
        if (totalBytes > 0) {
            X86AddImmediateInstruction add =
                new X86AddImmediateInstruction(
                    Operand.of(Register.RSP),
                    totalBytes
                );
            add.AddNumOfSpaceForPrefix(4);
            out.add(add);
            helper.addToCurrentFuncStackSize(-totalBytes);
        }

        // 5) Move result from RAX / XMM0 into `result`
        if (result != null) {
            if (result.GetVariableType() == VariableType.Double) {
                Register dst = helper.getRegisterFor(result);
                if (dst != null) {
                    X86MoveFPInstruction mv = new X86MoveFPInstruction(
                        /*isDouble=*/true,
                        Operand.of(dst),
                        Operand.of(Register.XMM0)
                    );
                    mv.AddNumOfSpaceForPrefix(4);
                    out.add(mv);
                } else {
                    helper.spillIfNeeded(result, Operand.of(Register.XMM0), out);
                }
            } else {
                Register dst = helper.getRegisterFor(result);
                if (dst != null) {
                    X86MoveInstruction mv = new X86MoveInstruction(
                        Operand.of(dst),
                        Operand.of(Register.RAX)
                    );
                    mv.AddNumOfSpaceForPrefix(4);
                    out.add(mv);
                } else {
                    helper.spillIfNeeded(result, Operand.of(Register.RAX), out);
                }
            }
        }

        // Lets restore save registers
        RegisterSaver.restore(helper, out, Register.callerSave(), helper.getLiveRegistersAtCurrentStep());

        helper.finishStep();
        return out;
    }
}
