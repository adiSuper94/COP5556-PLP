package edu.ufl.cise.plpfa21.assignment2;

import edu.ufl.cise.plpfa21.assignment1.IPLPLexer;
import edu.ufl.cise.plpfa21.assignment1.IPLPToken;
import edu.ufl.cise.plpfa21.assignment1.PLPTokenKinds;

public class RecDecParser implements IPLPParser{
    IPLPLexer lexer;
    public RecDecParser(IPLPLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public void parse() throws Exception {
        IPLPToken token = lexer.nextToken();
        while(token.getKind() != PLPTokenKinds.Kind.EOF){

        }

    }
}
