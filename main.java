import java_cup.runtime.*;
import javalette.*;
import javalette.Absyn.*;
import java.io.*;
import java.util.Scanner;

class Main
{
    public static void main(String[] args)
    {
        Yylex y = new Yylex(System.in);
        parser p = new parser(y, y.getSymbolFactory()) ;
    }
}