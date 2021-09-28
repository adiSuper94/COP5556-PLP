package edu.ufl.cise.plpfa21.assignment1;

import edu.ufl.cise.plpfa21.assignment2.IPLPParser;
import edu.ufl.cise.plpfa21.assignment2.RecDecParser;

public class CompilerComponentFactory {

    static IPLPLexer getLexer(String input) {
        //TODO  create and return a Lexer instance to parse the given input.
        return new DFALexer(input);
    }


    public static IPLPParser getParser(String input) {
        IPLPLexer lexer = getLexer(input);
        return new RecDecParser(lexer);
    }
}
