package jlc.main;

public class Main {

    public static void main(String args[]) {
        boolean doX86   = false;
        boolean doLLVM  = false;
        boolean verbose = false;
        String outX86   = "output.asm";
        String outLL    = "output.ll";

        for (String a : args) {
            switch (a) {
            case "--x86":
                doX86 = true;
                break;
            case "--llvm":
                doLLVM = true;
                break;
            case "-v": case "--v":
                verbose = true;
                break;
            default:
                System.err.println("Unknown flag: " + a);
                System.exit(1);
            }
        }
        
        // pick llvm if nothing appeard
        if (!doX86 && !doLLVM) {
            doLLVM = true;
        }

        try {
            TypeChecker typeChecker = new TypeChecker();
            typeChecker.performTypeCheckOnFile(System.in);

            if (doLLVM) {
                CodeGenerator codeGenerator = new CodeGenerator(
                    typeChecker.GetCtx(),
                    typeChecker.GetTree(),
                    outLL);
                codeGenerator.GenerateCode(true);
            }

            if (doX86) {
                X86CodeGenerator x86gen = new X86CodeGenerator(typeChecker.GetCtx(),
                                    typeChecker.parse_tree,
                                    outX86);
                x86gen.GenerateCode(true, verbose, verbose);
            }

        } catch(Exception e) {
            if (verbose) {
                System.err.println(String.format("failed to compile the given input:\n%s", e.getMessage()));
                e.printStackTrace();
            }
            System.err.println("ERROR");
            System.exit(1);
        } catch (Error e) {
            System.err.println("ERROR");
            System.exit(1);
        }

        System.err.println("OK");
    }
}
