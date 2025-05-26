package jlc.main;

public class Main {

    public static void main(String args[]) {
        try {
            TypeChecker typeChecker = new TypeChecker();
            typeChecker.performTypeCheckOnFile(System.in);

            // CodeGenerator codeGenerator = new CodeGenerator(
            //     typeChecker.GetCtx(),
            //     typeChecker.GetTree(),
            //     "output.ll");
            // codeGenerator.GenerateCode(true);
        } catch(Exception e) {
            if (args.length > 0 && args[0].equals(new String("v"))) {
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
