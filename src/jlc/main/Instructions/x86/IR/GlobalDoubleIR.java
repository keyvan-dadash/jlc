package jlc.main.Instructions.x86.IR;

import java.util.ArrayList;
import java.util.List;
import jlc.main.Instructions.Instruction;
import jlc.main.Instructions.x86.CodeGenHelper;
import jlc.main.Instructions.x86.LivenessAnalysis;
import jlc.main.Instructions.x86.Instructions.X86DirectiveInstruction;

/**
 * Represents a global double-precision constant in the x86 IR.
 *
 * Emits an IR directive of the form:
 *   GLOBALDOUBLE <label>, <value>
 *
 * Your backend can lower this to:
 *   <label>: .double <value>
 */
public class GlobalDoubleIR implements IR {
    private final String label;
    private final double value;

    /**
     * @param label the name of the global symbol (no leading '@' or '_')
     * @param value the IEEE-754 double value
     */
    public GlobalDoubleIR(String label, double value) {
        this.label = label;
        this.value = value;
    }

    @Override
    public String GetIR() {
        // Emit exactly the bit-value or decimal form as needed
        return String.format("GLOBALDOUBLE %s, %s", label, value);
    }

    @Override
    public void PerformLivenessAnalysis(LivenessAnalysis livenessAnalysis) {
        // no liveness for global constants
    }

    @Override
    public List<Instruction> GenerateX86Code(CodeGenHelper helper) {
        List<Instruction> out = new ArrayList<>();
        // emit the label
        out.add(new X86DirectiveInstruction(label + ":"));
        // emit the 64-bit value
        out.add(new X86DirectiveInstruction(String.format("    dq %s", value)));
        return out;
    }
}
