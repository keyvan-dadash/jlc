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

## Javalette language

For seeing the grammer of the language you should see the Javalette.cf file which has the specification of the language.

## List of shift/reduce conflicts

Currently, we only have one shift conflict which the if and else.

Here is the problem:

```txt
Cond.      Stmt ::= "if" "(" Expr ")" Stmt  ;
CondElse.  Stmt ::= "if" "(" Expr ")" Stmt "else" Stmt  ;
```

When we hit "else" token we have two options:
1. Reduce the inner if Expr then Stmt as a complete Stmt (i.e. conclude that there is no else for that inner if)
2. Shift the else, hoping to form the longer production if Expr else Stmt.

What we are doing is that we are shifting the else to the closest unmatched if statement. 

This is not harmful because of the specification in the language that else should be matched to the closet if statment.

## List of extension
We have implemented x86 backend so far. We use 64 bit registers.