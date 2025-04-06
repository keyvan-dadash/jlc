package jlc.main;

import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect.Type;

import jlc.lib.javalette.*;

public class TypeChecker {

  TypeChecker() {
        
  }

  public void performTypeCheckOnFile(InputStream stream) throws Exception {
    // Set up the lexer and parser to read from standard input.
    Yylex l = new Yylex(stream);
    parser p = new parser(l, l.getSymbolFactory());
    
    // Parse the input to obtain the AST (of type javalette.Absyn.Prog).
    jlc.lib.javalette.Absyn.Prog parse_tree = p.pProg();
    
    // Create an instance of VisitSkel and its ProgVisitor.
    FnVisit skel = new FnVisit();
    FnVisit.ProgVisitor tmp = skel.new ProgVisitor();
    Ctx fnCtx = new Ctx();
    tmp.SetCtx(fnCtx);
    jlc.lib.javalette.Absyn.Prog.Visitor<Ctx, Ctx> visitor = tmp;

    // Run the visitor on the parse tree.
    parse_tree.accept(visitor, null);

    TypeCheckerVisit skel_type = new TypeCheckerVisit();
    TypeCheckerVisit.ProgVisitor tmp_type = skel_type.new ProgVisitor();
    tmp_type.SetCtx(fnCtx);
    jlc.lib.javalette.Absyn.Prog.Visitor<Ctx, Ctx> visitor_type = tmp_type;

    // Run the visitor on the parse tree.
    parse_tree.accept(visitor_type, null);
  }
}
