package jlc.main.Instructions.x86.IR;

import java.util.*;
import java.util.stream.Collectors;
import jlc.main.Function;
import jlc.main.Variables.Variable;
import jlc.main.Instructions.x86.LivenessAnalysis;
import jlc.main.Instructions.x86.MemSize;
import jlc.main.Instructions.x86.Operand;
import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Address;
import jlc.main.Instructions.x86.CodeGenHelper;
import jlc.main.Instructions.x86.Register;
import jlc.main.Instructions.x86.RegisterSaver;
import jlc.main.Instructions.x86.Utils;
import jlc.main.Instructions.x86.Instructions.X86LabelInstruction;
import jlc.main.Instructions.x86.Instructions.X86MoveInstruction;
import jlc.main.Instructions.x86.Instructions.X86PushInstruction;
import jlc.main.Instructions.x86.Instructions.X86SubImmediateInstruction;

/**
 * Emits the IR for beginning a function definition, using your existing Function class.
 * Also computes stack offsets for parameters and locals and renames each Variable
 * to its stack-address string (e.g. “[ebp+8]” or “[ebp-4]”).
 */
public class FuncDef implements IR {
    private final Function func;
    private int totalParamSize;  // in bytes
    private int totalLocalSize;  // in bytes
    private int totalSpillSize;
    private int frameSize;
    private final Map<Variable,Integer> funcSpillMap = new LinkedHashMap<>();

    public FuncDef(Function function) {
        this.func = function;
    }

    @Override
    public String GetIR() {
        computeVariableAddresses(false);
        List<String> names = func.func_args.stream()
            .map(Variable::GetVariableName)
            .collect(Collectors.toList());
        String paramList = String.join(", ", names);
        String retType = func.GetReturn().GetVariableName();
        return String.format("FUNCDEF %s, %s%s%s",
            func.fn_name,
            retType,
            paramList.isEmpty() ? "" : ", ",
            paramList);
    }

    @Override
    public void PerformLivenessAnalysis(LivenessAnalysis livenessAnalysis) {
        // no-op for function header
    }

    /**
     * Compute and return a map from each parameter and local Variable
     * to its stack Address.  Parameters at [ebp+8], [ebp+12], …;
     * locals at [ebp-4], [ebp-8], …
     * Rounds every slot up to 4 bytes so that booleans still get 4-byte slots.
     */
    public Map<Variable,Address> computeVariableAddresses(boolean computeSpill) {
        Map<Variable,Address> addressMap = new LinkedHashMap<>();

        // Parameters: start at +8
        int offset = 16;
        for (Variable v : func.func_args) {
            int rawBytes = Utils.bitsForVariable(v) / 8;
            int slotBytes = (rawBytes % 8 == 0) ? rawBytes : 8;
            addressMap.put(v, new Address(Register.RBP, offset));
            offset += slotBytes;
        }
        totalParamSize = offset - 16;

        // Locals: start at -4, downward
        int localOffset = 0;
        for (Variable v : func.func_local) {
            int rawBytes = Utils.bitsForVariable(v) / 8;
            int slotBytes = (rawBytes % 8 == 0) ? rawBytes : 8;
            localOffset += slotBytes;
            addressMap.put(v, new Address(Register.RBP, -localOffset));
        }
        totalLocalSize = localOffset;

        for (Map.Entry<Variable,Address> e : addressMap.entrySet()) {
            e.getKey().SetVariableName(e.getValue().toString());
        }

        if (computeSpill) {
            int tempMajorOffset = 8;
            for (Map.Entry<Variable,Integer> e : funcSpillMap.entrySet()) {
                Variable v = e.getKey();
                int      tmpOffset = e.getValue();  // 0-based slot index
                int      so = -( totalLocalSize + tmpOffset + tempMajorOffset);
                addressMap.put(v, new Address(Register.RBP, so));
            }
        }

        return addressMap;
    }

    public void computeSpillVariables(Map<Variable,Integer> spillSlots, Map<String,Integer> funcFrameSizes, Map<String,Integer> funcParamSizes) {
        computeVariableAddresses(false);
        funcSpillMap.clear();
        int spillOffsetBytes = 8;
        for (Variable v : func.func_temps) {
            if (spillSlots.containsKey(v)) {
                int raw = Utils.bitsForVariable(v) / 8;
                int slot = (raw % 8 == 0 ? raw : 8);
                funcSpillMap.put(v, spillOffsetBytes);
                spillSlots.put(v, spillOffsetBytes);
                spillOffsetBytes += slot;
            }
        }
        totalSpillSize = spillOffsetBytes;

        //    sub rsp, frameSize
        frameSize = totalLocalSize + totalSpillSize;
        frameSize = Utils.alignTo16(frameSize);
        funcFrameSizes.put(func.fn_name, frameSize);
        funcParamSizes.put(func.fn_name, totalParamSize);
    }

    /** Total bytes reserved for parameters on the stack. */
    public int getTotalParamSize() {
        return totalParamSize;
    }

    /** Total bytes reserved for locals on the stack. */
    public int getTotalLocalSize() {
        return totalLocalSize;
    }

    @Override
    public List<Instruction> GenerateX86Code(CodeGenHelper codeGenHelper) {
        List<Instruction> out = new ArrayList<>();

        // Lets record the function and extract it used registers
        codeGenHelper.setCurrentFunc(func.fn_name);
        codeGenHelper.recordFunctionRegisters(func);

        // **Function label**
        // You’ll need an X86LabelInstruction (or similar) that just emits “<fn_name>:”
        out.add(new X86LabelInstruction(func.fn_name));

        // 3) Prologue
        //    push rbp
        X86PushInstruction ebpInstruction = new X86PushInstruction(Operand.of(Register.RBP));
        ebpInstruction.AddNumOfSpaceForPrefix(4);
        out.add(ebpInstruction);

        // Lets add func stack size because we pushed rbp and we need return address
        if (func.fn_name == "main") {
            codeGenHelper.addToCurrentFuncStackSize(8);
        } else {
            codeGenHelper.addToCurrentFuncStackSize(8 + 8);
        }

        //    mov rbp, rsp
        X86MoveInstruction x86MoveInstruction = new X86MoveInstruction(
            Operand.of(Register.RBP),
            Operand.of(Register.RSP)
        );
        x86MoveInstruction.AddNumOfSpaceForPrefix(4);
        out.add(x86MoveInstruction);
        
        if (frameSize > 0) {
            X86SubImmediateInstruction x86SubImmediateInstruction = new X86SubImmediateInstruction(
                Operand.of(Register.RSP),
                frameSize
            );

            // Lets add func stack size
            codeGenHelper.addToCurrentFuncStackSize(frameSize);
            // System.out.printf("Funcdef %s %d\n", func.fn_name, codeGenHelper.getCurrentFuncStackSize());

            x86SubImmediateInstruction.AddNumOfSpaceForPrefix(4);
            out.add(x86SubImmediateInstruction);
        }

        // Lets save callee registers
        if (func.fn_name != "main") {
            // System.out.println(func.fn_name);
            RegisterSaver.save(codeGenHelper, out, Register.calleeSave(), codeGenHelper.getFunctionRegisters(func.fn_name));
        }

        return out;
    }
}
