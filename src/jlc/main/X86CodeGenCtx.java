package jlc.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;

import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.IR.IR;
import jlc.main.Variables.Variable;

public class X86CodeGenCtx {

    // temp_variable_counter is a counter for creating a new temporary variable
    public int temp_variable_counter;

    // label_counter is a counter for creating a new label each time
    public int label_counter;

    // gloabl_variable_counter is a counter for creating global variables.
    public int gloabl_variable_counter;

    // rename_variable_counter is a counter for creating an indirect variable name
    // for renaming local variables.
    public int rename_variable_counter;

    // global_instructions holds instructions that should be put in the begining of the llvm output
    // such as putting the data of string
    public List<IR> global_instructions;

    // instruction_of_ctx holds normal instructions
    public List<IR> instruction_of_ctx;

    // last_incompelete_instruction holds instructions that we got from depth of the tree but we couldnt
    // add because it needs more information. Primary usecase of this is in add, mul and rel operations.
    private IR last_incompelete_instruction;

    // last_variable holds the result of last expression
    private Variable last_variable;

    // ctx_variables holds local variables of this ctx.
    // Note: this is a private attribute because we dont simply hold the variables of ctx.
    // Instead, we hold them by another name. For example, if we have variable named i 
    // in the function we rename it to var1_i. We have to do this in order to support 
    // shadow variables. In another word, we are doing is uniqueize the local variables.
    private Map<String, Variable> ctx_variables;

    // mapped_varibles holds the map of the real local variables to the renamed ones.
    private Map<String, String> mapped_varibles;

    // loaded_variables holds map of local variables to the temporary variables that
    // have loaded the local variable because we hold all the local variables as pointer so we
    // need to load them into temporary variables. Thus, we store the last temporary variable
    // that loaded each local variables. Upon each store operation, we look into this map 
    // and invalidate the temporary variable since that temporary variable has a value that is 
    // not longer as same as the local variable since we store something new in the local variable.
    // Therefore, we need to load the local variable again but in a new temporary variable.
    private Map<String, Variable> loaded_variables;
    
    // functions keeps declared function of ctx.
    // we need this in order to form the defenition of functions
    // in llvm.
    public Map<String, Function> functions;

    // We need to keep the map of global strings name to their 
    // content.
    public Map<String, String> global_strings;

    // parent shows what is the parent of ctx. This is need 
    // because sometimes we generate a new ctx but still we need
    // to search for variables and these stuff in our parents.
    private X86CodeGenCtx parent;

    // is_ctx_return will tell us whether the ctx return or not.
    // This is important in the code generation of if and else.
    public Boolean is_ctx_return;

    // jmp_labels holds final jmp label of "and" and "or" in order to suppoer
    // nester "and" and "or" using phi instruction.
    public Stack<String> jmp_labels;

    // current_func shows that which function the current context is processing
    public Function current_func;

    public Variable logic_local_variable;

    public Variable lastLocalVariable;

    X86CodeGenCtx() {
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
        jmp_labels = new Stack<>();
        current_func = null;
        logic_local_variable = null;
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
        if (this.current_func != null) {
            this.current_func.func_temps.add(tempVariable);
        }
        
        return tempVariable;
    }

    // GetNewLabel returns a unique label.
    public String GetNewLabel() {
        return String.format("label_%s", String.valueOf(label_counter++));
    }

    // GetNewLabelWithPrefix returns a unique label with each prefix.
    // Note: list of prefixes should be unique because we only increase our
    // label counter only after we generated label for each of this prefixes.
    public List<String> GetNewLabelWithPrefix(String... prefixs) {
        List<String> labels = new ArrayList<>();
        for (String prefix : prefixs) {
            labels.add(String.format("%s_%s", prefix, String.valueOf(label_counter)));
        }

        // Now, lets increase the counter.
        label_counter++;
        return labels;
    }

    // ResetCounters resets the counters that this class use for creating unique label, temporary vairable
    // and renaming variable.
    public void ResetCounters() {
        temp_variable_counter = 0;
        label_counter = 0;
        rename_variable_counter = 0;

        // We shouldnt reset global counter since
        // the global counter should be consistent
        // among all the ctx.
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
    public void SetLastInstruction(IR ins) {
        assert(last_incompelete_instruction == null);
        last_incompelete_instruction = ins;
    }

    // GetLastInstruction gets the last incompelete instruction.
    public IR GetLastInstruction() {
        assert(last_incompelete_instruction != null);
        return last_incompelete_instruction;
    }

    // ClearLastInstruction clears last incompelete instruction.
    public void ClearLastInstruction() {
        assert(last_incompelete_instruction != null);
        last_incompelete_instruction = null;
    }

    // CopyInstructionsFromCtx copies instructions from other ctx.
    public void CopyInstructionsFromCtx(X86CodeGenCtx ctx) {
        this.global_instructions.addAll(ctx.global_instructions);
        this.instruction_of_ctx.addAll(ctx.instruction_of_ctx);
        this.global_strings.putAll(ctx.global_strings);
    }

    public void CopyCountersFromCtx(X86CodeGenCtx ctx) {
        this.temp_variable_counter = ctx.temp_variable_counter;
        this.label_counter = ctx.label_counter;
        this.rename_variable_counter = ctx.rename_variable_counter;
        this.gloabl_variable_counter = ctx.gloabl_variable_counter;
    }

    // GetRenamedVariable returns a new name for the vairable
    // this is because we need a new name so that we support shadow variables.
    // Note: if this API called multiple times we might endup with the same name.
    // We only increase our renamed counter after the calling of AddToCtxVariable.
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

    // extractVariableNameFromRenamedName will extract a the name of the original variable
    // from the renamed variable. Here is how we extract:
    // Every renamed variable is like this: var(number)_(name of original variable)
    // For example:
    //          var2_r
    //          var4_name
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

    // AddVariabelAsLoaded will put the temporary variable that loaded the local variable
    // inside the context.
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
            System.out.println(loaded_variables.size());
        }
    }

    // GetVariableIfLoaded tries to find the temporary variable
    // that loaded the local variable. If not it will return null.
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

    // UnloadVariable will remove the temporary variable that loaded this local variable.
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
    public static X86CodeGenCtx GetSubCtxWithVariables(X86CodeGenCtx ctx) {
        X86CodeGenCtx subCtx = new X86CodeGenCtx();
        subCtx.temp_variable_counter = ctx.temp_variable_counter;
        subCtx.label_counter = ctx.label_counter;
        subCtx.rename_variable_counter = ctx.rename_variable_counter;
        subCtx.ctx_variables = ctx.ctx_variables;
        subCtx.loaded_variables = ctx.loaded_variables;
        subCtx.functions = ctx.functions;
        subCtx.current_func = ctx.current_func;
        subCtx.logic_local_variable = ctx.logic_local_variable;

        // This is something that needs to be consistent in all subctx
        subCtx.gloabl_variable_counter = ctx.gloabl_variable_counter;
        subCtx.parent = ctx;
        return subCtx;
    }

    // GetSubCtxWithoutVariables gets new ctx from the given ctx but without any varibles.
    public static X86CodeGenCtx GetSubCtxWithoutVariables(X86CodeGenCtx ctx) {
        X86CodeGenCtx subCtx = new X86CodeGenCtx();
        subCtx.temp_variable_counter = ctx.temp_variable_counter;
        subCtx.label_counter = ctx.label_counter;
        subCtx.rename_variable_counter = ctx.rename_variable_counter;
        subCtx.functions = ctx.functions;
        subCtx.current_func = ctx.current_func;
        subCtx.logic_local_variable = ctx.logic_local_variable;

        // This is something that needs to be consistent in all subctx
        subCtx.gloabl_variable_counter = ctx.gloabl_variable_counter;
        subCtx.parent = ctx;
        return subCtx;
    }

    // GetSubCtxWithoutVariables gets new ctx from the given ctx but without any varibles and parent.
    public static X86CodeGenCtx GetSubCtxWithoutVariablesAndParent(X86CodeGenCtx ctx) {
        X86CodeGenCtx subCtx = new X86CodeGenCtx();
        subCtx.functions = ctx.functions;
        subCtx.current_func = ctx.current_func;
        subCtx.logic_local_variable = ctx.logic_local_variable;

        // For x86, we have a small problem which is register allocation need to be assigned to unique variable names
        subCtx.CopyCountersFromCtx(ctx);

        // This is something that needs to be consistent in all subctx
        subCtx.gloabl_variable_counter = ctx.gloabl_variable_counter;
        subCtx.parent = null;
        return subCtx;
    }
    
    // Lets try to find what is the loaded variable crrosponding local variable
    public Variable findLoadedVariableName(Variable var) {
        if (parent != null) {
            Variable original = parent.findLoadedVariableName(var);
            if (original != null) {
                return original;
            }
        }

        String renamed = null;
        // System.out.printf("%s %d", var.GetVariableName(), loaded_variables.size());
        for (Map.Entry<String, Variable> e : loaded_variables.entrySet()) {
            // System.out.printf("k:%s v:%s\n", e.getKey(), e.getValue());
            if (e.getValue().equals(var)) {
                renamed = e.getKey();
                break;
            }
        }
        if (renamed == null) return null;

        String original = null;
        for (Map.Entry<String, String> e : mapped_varibles.entrySet()) {
            // System.out.printf("k:%s v:%s\n", e.getKey(), e.getValue());
            if (renamed.equals(e.getValue())) {
                original = e.getKey();
                break;
            }
        }
        if (original == null) return null;

        return ctx_variables.get(original);
    }
}
