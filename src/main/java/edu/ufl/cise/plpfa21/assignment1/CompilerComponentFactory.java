package edu.ufl.cise.plpfa21.assignment1;

import edu.ufl.cise.plpfa21.assignment2.IPLPParser;
import edu.ufl.cise.plpfa21.assignment2.RecDecParser;

public class CompilerComponentFactory {

    static IPLPLexer getLexer(String input) {
        return new DFALexer(input);
    }


    public static IPLPParser getParser(String input) {
        //TODO : Return new parser that returns AST when parse method is called.
        return new RecDecParser(new DFALexer(input));
    }
}
