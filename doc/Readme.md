# JLC Compiler

## How to use

First step is to export classpath to makesure java cup and lexer is presented:

```sh
export CLASSPATH=.:${pwd}/bin:{classpath that has java cup and lexer}
```

Second is that building the compiler with this command:

```sh
make
```

After that, we can run the compiler with the following command

```sh
java jlc.main.Main < src/test.jl
```

Options that can be used for this compiler is this:

```sh
--llvm // Generate llvm code
--x64 // Generate x64 assembly code
--v // Verbose information
```

The testsuite can be run by
```sh
 python3 testing.py ./jlc --llvm -x arrays1
```
## Javalette language

For seeing the grammer of the language you should see the Javalette.cf file which has the specification of the language.

In our language we have a redundant expression, the statement **ArrAssExpr** is never actually used in parsing the language. However, it is still a part of our project as we are low on time and it is just redundant and not in any way detrimental.

## List of shift/reduce conflicts

During parser generation with JavaCUP, a few shift/reduce conflicts are reported. These are expected and non-problematic due to the intended structure of the grammar and how the parser works.
A shift/reduce conflict arises when the parser can either:

* Shift the next token

* Reduce the current input

Why This Isn't a Problem in our parser, because Java Cup is a LALR(1) parser, which resolves conflicts automatically and deterministically using a lookahead token and the current parser state. The parser usually resolves these by shifting instead of reducing.

### Dangling else conflict

```txt
Stmt ::= "if" "(" Expr ")" Stmt ;
Stmt ::= "if" "(" Expr ")" Stmt "else" Stmt ;
```

When the parser sees an else, it must choose between:

* Reducing the preceding if (Expr) Stmt as a complete Stmt, assuming no else is present.

* Shifting the else, assuming the full form if (Expr) Stmt else Stmt applies.

We resolve this by shifting the else, meaning it associates with the nearest unmatched if. This is the standard resolution strategy and works with the javalette language.

This conflict is harmless and expected in most languages that support optional else clauses, including C and Java.

### Identifier-Based ambiguity

```txt
Shift/Reduce conflict found in state #31
  between Expr6 ::= _IDENT_ (*)
  and     Stmt ::= _IDENT_ (*) _SYMB_7 Expr _SYMB_8 _SYMB_6 Expr _SYMB_5
  and     Stmt ::= _IDENT_ (*) _SYMB_7 Expr _SYMB_8 _SYMB_11 _SYMB_5
  and     Stmt ::= _IDENT_ (*) _SYMB_7 Expr _SYMB_8 _SYMB_10 _SYMB_5
  under symbol _SYMB_7
  Resolved in favor of shifting.
```

Here, the parser must decide whether _IDENT_ is an expression or the start of an assignment statement. JavaCUP resolves this by shifting, favoring the longer rule that forms a complete statement.

This behavior is also safe and aligns with the intended grammar, where standalone identifiers are typically used in assignment contexts.

### Block statement amibiguity

```txt
Shift/Reduce conflict found in state #127
  between Stmt ::= "while" "(" Expr ")" Stmt (*)
  and     Stmt ::= "while" "(" Expr ")" Stmt (*) "else" Stmt
  under symbol "else"
  Resolved in favor of shifting.
```

In this case, the parser must decide whether the statement after a control structure like while is complete, or whether it will be followed by an else. The conflict is resolved by shifting, again favoring the longer and more complete structure. This is safe and aligns with conventional parsing strategies.

## List of extension
* We have implemented x86 backend. We use 64 bit registers. 
* We also implemented Arrays1

