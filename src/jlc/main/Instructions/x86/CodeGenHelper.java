package jlc.main.Instructions.x86;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jlc.main.Variables.Variable;
import jlc.main.Variables.VariableKind;
import jlc.main.Variables.VariableType;
import jlc.main.Instructions.x86.LinearScanAllocator.AssignedInterval;
import jlc.main.Function;
import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.Instructions.X86MoveFPInstruction;
import jlc.main.Instructions.x86.Instructions.X86MoveInstruction;
import jlc.main.Instructions.x86.LinearScanAllocator.AllocationResult;

/**
 * Helper for generating x86 code from IR, using the register allocation result.
 */
public class CodeGenHelper {
    private final Map<Variable, List<AssignedInterval>> assignments;
    private final Map<Variable,Integer> spillSlots;
    private final Map<Integer,List<Variable>> spillsByStep;
    private final Map<String,Integer> funcFrameSizes;
    private final Map<String,Integer> funcArgSizes = new HashMap<>();
    private final Map<String, Set<Register>> funcRegisters = new HashMap<>();

    private String current_func = "";
    private Integer current_func_stack_size = 0;
    private int currentStep;

    public CodeGenHelper(AllocationResult allocationResult, Map<Variable,Integer> spillSlots, Map<Integer,List<Variable>> spillsByStep) {
        this.assignments = allocationResult.map;
        this.spillSlots = spillSlots;
        this.funcFrameSizes = new HashMap<>();
        this.currentStep = 0;
        this.spillsByStep = spillsByStep;
    }

    public void finishStep() {
        currentStep++;
    }

    public Register getRegisterFor(Variable var) {
        List<AssignedInterval> intervals = assignments.get(var);
        if (intervals == null) {
            return null;
        }
        for (AssignedInterval iv : intervals) {
            // System.out.println(iv.start);
            // System.out.println(iv.end);
            // System.out.println(currentStep);
            if (currentStep >= iv.start && currentStep <= iv.end) {
                return iv.reg;
            }
        }
        return null;
    }

    public Map<Variable,Integer> getSpillSlots() {
        return spillSlots;
    }

    public Operand ensureInRegister(Variable v, List<Instruction> out) {
        if (VariableKind.ConstantVariable == v.GetVariableKind()) {
            return Operand.ofImmediate(v);
        }

        // System.out.println(v.GetVariableName());    

        Register r = getRegisterFor(v);
        if (r != null) {
            return Operand.of(r);
        }

        // spilled: pick scratch
        if (v.GetVariableType() == VariableType.Double) {
            Register sx = Register.xmmScratch();
            int slot = spillSlots.get(v);
            Address addr = new Address(Register.RBP, -slot);
            // movsd xmmX, [rbp - slot]
            X86MoveFPInstruction ld = new X86MoveFPInstruction(
                true,
                Operand.of(sx), Operand.of(addr)
            );
            ld.AddNumOfSpaceForPrefix(4);
            out.add(ld);
            return Operand.of(sx);

        } else {
            Register sx = Register.gpScratch();
            int slot = spillSlots.get(v);
            Address addr = new Address(Register.RBP, -slot);
            // mov spillReg, [rbp - slot]
            X86MoveInstruction ld = new X86MoveInstruction(
                Operand.of(sx), Operand.of(addr)
            );
            ld.AddNumOfSpaceForPrefix(4);
            out.add(ld);
            return Operand.of(sx);
        }
    }

    public void spillIfNeeded(Variable dest, Operand op, List<Instruction> out) {
        if (VariableKind.ConstantVariable == dest.GetVariableKind()) {
            return;
        }

        if (Utils.isAddressVariable(dest)) {
            return;
        }

        if (getRegisterFor(dest) != null) return;
        int slot = spillSlots.get(dest);
        Address addr = new Address(Register.RBP, -slot);
        if (dest.GetVariableType() == VariableType.Double) {
            // movsd [rbp - slot], xmmX
            X86MoveFPInstruction st = new X86MoveFPInstruction(
                true,
                Operand.of(addr), op
            );
            st.AddNumOfSpaceForPrefix(4);
            out.add(st);
        } else {
            // mov [rbp - slot], spillReg
            X86MoveInstruction st = new X86MoveInstruction(
                Operand.of(addr), op
            );
            st.AddNumOfSpaceForPrefix(4);
            out.add(st);
        }
    }

    public Map<String,Integer> getFuncFrames() {
        return funcFrameSizes;
    }

    public Map<String,Integer> getFuncArgSize() {
        return funcArgSizes;
    }

    public String getCurrentFunc() {
        return current_func;
    }

    public void setCurrentFunc(String func_name) {
        current_func = func_name;
        current_func_stack_size = 0;
    }

    public Integer getCurrentFuncStackSize() {
        return current_func_stack_size;
    }

    public void addToCurrentFuncStackSize(Integer size) {
        current_func_stack_size += size;
    }

    public void recordFunctionRegisters(Function function) {
        String fn = function.fn_name;
        Set<Register> regs = new LinkedHashSet<>();
        
        for (Variable v : function.func_temps) {
            List<AssignedInterval> intervals = assignments.get(v);
            if (intervals != null) {
                for (AssignedInterval iv : intervals) {
                    if (iv.reg != null) {
                        regs.add(iv.reg);
                    }
                }
            }
            if (spillSlots.get(v) != null) {
                if (v.GetVariableType() == VariableType.Double) {
                    regs.add(Register.xmmScratch());
                } else {
                    regs.add(Register.gpScratch());
                }
            }
        }
        funcRegisters.put(fn, regs);
    }

    public Set<Register> getLiveRegistersAtCurrentStep() {
        Set<Register> live = new LinkedHashSet<>();
        for (List<AssignedInterval> intervals : assignments.values()) {
            for (AssignedInterval iv : intervals) {
                if (iv.reg != null
                    && currentStep > iv.start
                    && currentStep < iv.end) {
                    live.add(iv.reg);
                }
            }
        }
        return live;
    }

    public Set<Register> getFunctionRegisters(String functionName) {
        return funcRegisters.getOrDefault(functionName, Collections.emptySet());
    }

    public void spillCurrentStep(List<Instruction> out) {
        List<Variable> raw = spillsByStep.get(currentStep);
        if (raw == null) return;
        List<Variable> toSpill = raw.stream()
                                .distinct()
                                .collect(Collectors.toList());

        // System.out.println(toSpill.stream()
        //     .map(Variable::GetVariableName)
        //     .collect(Collectors.joining(", ", "toSpill=[", "]")));
        // System.out.println(currentStep);
        for (Variable v : toSpill) {
            // if v happens to still be in a register, write it back
            Register r = getRegisterFor(v);
            if (r != null) {
                currentStep++;
                spillIfNeeded(v, Operand.of(r), out);
                currentStep--;
            }
        }
    }
}
