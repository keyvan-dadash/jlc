package jlc.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jlc.main.Instructions.Instruction;
import jlc.main.Variables.Variable;

public class LLVMCodeGenCtx {

    // temp_variable_counter is the counter for creating a new temporary variable
    public int temp_variable_counter;

    // label_counter is the counter for creating a new label each time
    public int label_counter;

    // gloabl_variable_counter is the counter for creating global variables.
    public int gloabl_variable_counter;

    // rename_variable_counter is counter for creating an indirect variable name
    // for renaming local variables.
    public int rename_variable_counter;

    // global_instructions holds instructions that should be put in the begining of the llvm output
    public List<Instruction> global_instructions;

    // instruction_of_ctx holds normal instructions
    public List<Instruction> instruction_of_ctx;

    // last_incompelete_instruction holds instructions that we got from depth of the tree but we couldnt
    // add because it needs more information. Primary usecase of this is in add, mul and rel operations.
    private Instruction last_incompelete_instruction;

    // last_variable holds the result of last expression
    private Variable last_variable;

    // ctx_variables holds local variables of this ctx.
    // Note: this is a private attribute because we dont hold the variable simply.
    // Instead we hold them as another name. For example, if we have variable named i 
    // in the function we rename it to var1_i. We have to do this in order to support 
    // shadow variables.
    private Map<String, Variable> ctx_variables;

    // mapped_varibles holds the map of variables to the renamed variables.
    private Map<String, String> mapped_varibles;

    // loaded_variables holds map of local variables to the temporary variables that
    // have loaded this value because we hold all the local variables as pointer so we
    // need to load them into temporary variables.
    private Map<String, Variable> loaded_variables;
    
    // functions keeps declared function of ctx.
    public Map<String, Function> functions;

    public Map<String, String> global_strings;

    private LLVMCodeGenCtx parent;

    // is_ctx_return will tell us whether the ctx return or not.
    // This is important in the code generation of if and else.
    public Boolean is_ctx_return;

    LLVMCodeGenCtx() {
        temp_variable_counter = 0;
        label_counter = 0;
        rename_variable_counter = 0;
        instruction_of_ctx = new ArrayList<>();
        global_instructions = new ArrayList<>();
        ctx_variables = new HashMap<>();
        loaded_variables = new HashMap<>();
        mapped_varibles = new HashMap<>();
        global_strings = new HashMap<>();
        last_incompelete_instruction = null;
        last_variable = null;
        parent = null;
        is_ctx_return = false;
    }

    // GetNewTempVairableWithTheSameTypeOf returns a new unique temporary variable.
    public Variable GetNewGlobalVairableWithTheSameTypeOf(Variable var) {
        Variable gVariable = var.GetNewVariableSameType();
        gVariable.SetVariableName("g" + String.valueOf(gloabl_variable_counter++));
        return gVariable;
    }

    // GetNewTempVairableWithTheSameTypeOf returns a new unique temporary variable.
    public Variable GetNewTempVairableWithTheSameTypeOf(Variable var) {
        Variable tempVariable = var.GetNewVariableSameType();
        tempVariable.SetVariableName("t" + String.valueOf(temp_variable_counter++));
        return tempVariable;
    }

    // GetNewLabel returns a unique label.
    public String GetNewLabel() {
        return String.format("label_%s", String.valueOf(label_counter++));
    }

    // GetNewLabelWithPrefix returns a unique label with prefix.
    public String GetNewLabelWithPrefix(String prefix) {
        return String.format("%s_%s", prefix, String.valueOf(label_counter++));
    }

    // ResetCounters resets the counters that this class use for creating unique label, temporary vairable
    // and renaming variable.
    public void ResetCounters() {
        temp_variable_counter = 0;
        label_counter = 0;
        rename_variable_counter = 0;
    }

    // SetLastVariable sets last variable.
    public void SetLastVariable(Variable var) {
        assert(last_variable == null);
        last_variable = var;
    }

    // GetLastVariable gets last variable.
    public Variable GetLastVariable() {
        assert(last_variable != null);
        return last_variable;
    }

    // ClearLastVariable clears the last variable.
    public void ClearLastVariable() {
        assert(last_variable != null);
        last_variable = null;
    }

    // SetLastInstruction sets the last incompelete instruction.
    public void SetLastInstruction(Instruction ins) {
        assert(last_incompelete_instruction == null);
        last_incompelete_instruction = ins;
    }

    // GetLastInstruction gets the last incompelete instruction.
    public Instruction GetLastInstruction() {
        assert(last_incompelete_instruction != null);
        return last_incompelete_instruction;
    }

    // ClearLastInstruction clears last incompelete instruction.
    public void ClearLastInstruction() {
        assert(last_incompelete_instruction != null);
        last_incompelete_instruction = null;
    }

    // CopyInstructionsFromCtx copies instructions from other ctx.
    public void CopyInstructionsFromCtx(LLVMCodeGenCtx ctx) {
        this.global_instructions.addAll(ctx.global_instructions);
        this.instruction_of_ctx.addAll(ctx.instruction_of_ctx);
        this.global_strings.putAll(ctx.global_strings);
    }

    public void CopyCountersFromCtx(LLVMCodeGenCtx ctx) {
        this.temp_variable_counter = ctx.temp_variable_counter;
        this.label_counter = ctx.label_counter;
        this.rename_variable_counter = ctx.rename_variable_counter;
        this.gloabl_variable_counter = ctx.gloabl_variable_counter;
    }

    public Variable GetRenamedVariable(Variable var) {
        String renameVar = "var" + String.valueOf(rename_variable_counter) + "_" + var.GetVariableName();
        Variable renameVariable = var.GetNewVariableSameType();
        renameVariable.SetVariableName(renameVar);
        return renameVariable;
    }

    // AddToCtxVariable adds a new variable to the ctx.
    // This variable should be a renamed variable.
    public void AddToCtxVariable(Variable var) {
        // First we should create a rename variable.
        rename_variable_counter++;

        // Lets extract the original variable name:
        String originalVariableName = extractVariableNameFromRenamedName(var.GetVariableName());
        mapped_varibles.put(originalVariableName, var.GetVariableName());
        ctx_variables.put(var.GetVariableName(), var);
    }

    private String extractVariableNameFromRenamedName(String input) {
        int underscoreIndex = input.indexOf("_");
        if (underscoreIndex != -1 && underscoreIndex < input.length() - 1) {
            return input.substring(underscoreIndex + 1);
        }
        throw new RuntimeException("we should get a renamed variable");
    }

    // GetVariableFromCtx gets a variable from this ctx. If the variable does
    // not exist it will check its parents.
    public Variable GetVariableFromCtx(String variableName) {
        // First we should renamed name
        String renamedVar = mapped_varibles.get(variableName);
        if (renamedVar == null) {
            // Lets check our parents.
            if (parent != null) {
                return parent.GetVariableFromCtx(variableName);
            }
        }

        // This eventually returns the renamed variable or null, which means that variable does not exist.
        return ctx_variables.get(renamedVar);
    }

    public void AddVariabelAsLoaded(String loadedVariableName, Variable tempVariable) {
        // First we should renamed name
        String renamedVar = mapped_varibles.get(loadedVariableName);
        if (renamedVar == null) {
            // This variable does not belong to use, so we need to load in our parents ctx.
            if (parent != null) {
                parent.AddVariabelAsLoaded(loadedVariableName, tempVariable);
            }
        } else {
            loaded_variables.put(renamedVar, tempVariable);
        }
    }

    public Variable GetVariableIfLoaded(String loadedVariable) {
        // First we should renamed name
        String renamedVar = mapped_varibles.get(loadedVariable);
        if (renamedVar == null) {
            // This variable does not belong to use, so we need to load in our parents ctx.
            if (parent != null) {
                return parent.GetVariableIfLoaded(loadedVariable);
            }
        }

        // This eventually returns the loaded variable or null, which means that variable has not been loaded.
        return loaded_variables.get(renamedVar);
    }

    public void UnloadVariable(String variableToUnload) {
        // First we should renamed name
        String renamedVar = mapped_varibles.get(variableToUnload);
        if (renamedVar == null) {
            // This variable does not belong to use, so we need to load in our parents ctx.
            if (parent != null) {
                parent.UnloadVariable(variableToUnload);
            }
        } else {
            loaded_variables.remove(variableToUnload);
        }
    }

    // GetSubCtxWithVariables gets new ctx that is sub ctx of the input ctx.
    // Currently there is no usecase for this.
    public static LLVMCodeGenCtx GetSubCtxWithVariables(LLVMCodeGenCtx ctx) {
        LLVMCodeGenCtx subCtx = new LLVMCodeGenCtx();
        subCtx.temp_variable_counter = ctx.temp_variable_counter;
        subCtx.label_counter = ctx.label_counter;
        subCtx.rename_variable_counter = ctx.rename_variable_counter;
        subCtx.ctx_variables = ctx.ctx_variables;
        subCtx.loaded_variables = ctx.loaded_variables;
        subCtx.functions = ctx.functions;

        // This is something that needs to be consistent in all subctx
        subCtx.gloabl_variable_counter = ctx.gloabl_variable_counter;
        subCtx.parent = ctx;
        return subCtx;
    }

    // GetSubCtxWithoutVariables gets new ctx from the given ctx but without any varibles.
    public static LLVMCodeGenCtx GetSubCtxWithoutVariables(LLVMCodeGenCtx ctx) {
        LLVMCodeGenCtx subCtx = new LLVMCodeGenCtx();
        subCtx.temp_variable_counter = ctx.temp_variable_counter;
        subCtx.label_counter = ctx.label_counter;
        subCtx.rename_variable_counter = ctx.rename_variable_counter;
        subCtx.functions = ctx.functions;

        // This is something that needs to be consistent in all subctx
        subCtx.gloabl_variable_counter = ctx.gloabl_variable_counter;
        subCtx.parent = ctx;
        return subCtx;
    }

    // GetSubCtxWithoutVariables gets new ctx from the given ctx but without any varibles and parent.
    public static LLVMCodeGenCtx GetSubCtxWithoutVariablesAndParent(LLVMCodeGenCtx ctx) {
        LLVMCodeGenCtx subCtx = new LLVMCodeGenCtx();
        subCtx.functions = ctx.functions;

        // This is something that needs to be consistent in all subctx
        subCtx.gloabl_variable_counter = ctx.gloabl_variable_counter;
        subCtx.parent = null;
        return subCtx;
    }
}
