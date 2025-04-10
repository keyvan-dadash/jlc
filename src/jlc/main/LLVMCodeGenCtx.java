package jlc.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jlc.main.Instructions.Instruction;
import jlc.main.Variables.Variable;

public class LLVMCodeGenCtx {
    public int temp_variable_counter;
    public int label_counter;
    public List<Instruction> global_instructions;
    public List<Instruction> instruction_of_ctx;
    public Instruction last_incompelete_instruction;
    public Variable last_variable;
    public Map<String, Variable> ctx_variables;
    public Map<String, Variable> loaded_variables;
    
    // functions keeps declared function of ctx.
    public Map<String, Function> functions;

    LLVMCodeGenCtx() {
        temp_variable_counter = 0;
        label_counter = 0;
        instruction_of_ctx = new ArrayList<>();
        global_instructions = new ArrayList<>();
        ctx_variables = new HashMap<>();
        loaded_variables = new HashMap<>();
        last_incompelete_instruction = null;
        last_variable = null;
    }

    public Variable GetNewTempVairableWithTheSameTypeOf(Variable var) {
        Variable tempVariable = var.GetNewVariableSameType();
        tempVariable.SetVariableName("t" + String.valueOf(temp_variable_counter++));
        return tempVariable;
    }

    public String GetNewLabel() {
        return String.format("label_%s", String.valueOf(label_counter++));
    }

    public String GetNewLabelWithPrefix(String prefix) {
        return String.format("%s_%s", prefix, String.valueOf(label_counter++));
    }

    public void ResetCounters() {
        temp_variable_counter = 0;
        label_counter = 0;
    }

    public void SetLastVariable(Variable var) {
        assert(last_variable == null);
        last_variable = var;
    }

    public Variable GetLastVariable() {
        assert(last_variable != null);
        return last_variable;
    }

    public void ClearLastVariable() {
        assert(last_variable != null);
        last_variable = null;
    }

    public void SetLastInstruction(Instruction ins) {
        assert(last_incompelete_instruction == null);
        last_incompelete_instruction = ins;
    }

    public Instruction GetLastInstruction() {
        assert(last_incompelete_instruction != null);
        return last_incompelete_instruction;
    }

    public void ClearLastInstruction() {
        assert(last_incompelete_instruction != null);
        last_incompelete_instruction = null;
    }

    public void CopyInstructionsFromCtx(LLVMCodeGenCtx ctx) {
        this.global_instructions.addAll(ctx.global_instructions);
        this.instruction_of_ctx.addAll(ctx.instruction_of_ctx);
    }

    public static LLVMCodeGenCtx GetSubCtxWithVariables(LLVMCodeGenCtx ctx) {
        LLVMCodeGenCtx subCtx = new LLVMCodeGenCtx();
        subCtx.temp_variable_counter = ctx.temp_variable_counter;
        subCtx.label_counter = ctx.label_counter;
        subCtx.ctx_variables = ctx.ctx_variables;
        subCtx.loaded_variables = ctx.loaded_variables;
        subCtx.functions = ctx.functions;
        return subCtx;
    }

    public static LLVMCodeGenCtx GetSubCtxWithoutVariables(LLVMCodeGenCtx ctx) {
        LLVMCodeGenCtx subCtx = new LLVMCodeGenCtx();
        subCtx.temp_variable_counter = ctx.temp_variable_counter;
        subCtx.label_counter = ctx.label_counter;
        subCtx.functions = ctx.functions;
        return subCtx;
    }
}
