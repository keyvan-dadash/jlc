package javalette;

import java.lang.ProcessBuilder.Redirect.Type;

public class Interpret {
  public static void main(String args[]) throws Exception {
    // Set up the lexer and parser to read from standard input.
    Yylex l = new Yylex(System.in);
    parser p = new parser(l, l.getSymbolFactory());
    
    // Parse the input to obtain the AST (of type javalette.Absyn.Prog).
    javalette.Absyn.Prog parse_tree = p.pProg();
    
    System.out.println("Pretty printed program:");
    System.out.println(PrettyPrinter.show(parse_tree));
    
    // Create an instance of VisitSkel and its ProgVisitor.
    FnVisit skel = new FnVisit();
    javalette.FnVisit.ProgVisitor tmp = skel.new ProgVisitor();
    Ctx fnCtx = new Ctx();
    tmp.SetCtx(fnCtx);
    javalette.Absyn.Prog.Visitor<Ctx, Ctx> visitor = tmp;

    // Run the visitor on the parse tree.
    parse_tree.accept(visitor, null);

    for (Function fn : fnCtx.functions.values()) {
        System.out.println(fn.fn_name);
    }


    TypeCheckerVisit skel_type = new TypeCheckerVisit();
    javalette.TypeCheckerVisit.ProgVisitor tmp_type = skel_type.new ProgVisitor();
    tmp_type.SetCtx(fnCtx);
    javalette.Absyn.Prog.Visitor<Ctx, Ctx> visitor_type = tmp_type;

    // Run the visitor on the parse tree.
    parse_tree.accept(visitor_type, null);
    
  }
}
