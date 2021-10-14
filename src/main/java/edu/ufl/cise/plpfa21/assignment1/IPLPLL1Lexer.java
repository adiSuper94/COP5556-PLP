package edu.ufl.cise.plpfa21.assignment1;

public interface IPLPLL1Lexer extends IPLPLexer{

    IPLPToken peekNextToken() throws LexicalException;
}
