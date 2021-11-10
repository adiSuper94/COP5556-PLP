package edu.ufl.cise.plpfa21.assignment1;

import edu.ufl.cise.plpfa21.assignment2.IPLPParser;
import edu.ufl.cise.plpfa21.assignment2.RecDecParser;
import edu.ufl.cise.plpfa21.assignment3.ast.ASTVisitor;
import edu.ufl.cise.plpfa21.assignment4.TypeCheckVisitor;

public class CompilerComponentFactory {

    static IPLPLexer getLexer(String input) {
        return new DFALexer(input);
    }


    public static IPLPParser getParser(String input) {
        return new RecDecParser(new DFALexer(input));
    }

    public static ASTVisitor getTypeCheckVisitor() {
        return new TypeCheckVisitor();
    }
}
