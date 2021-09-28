package edu.ufl.cise.plpfa21.assignment2;

import edu.ufl.cise.plpfa21.assignment1.IPLPLexer;
import edu.ufl.cise.plpfa21.assignment1.IPLPToken;
import edu.ufl.cise.plpfa21.assignment1.LexicalException;
import edu.ufl.cise.plpfa21.assignment1.PLPTokenKinds;

import java.util.ArrayList;
import java.util.List;

public class RecDecParser implements IPLPParser{
    IPLPLexer lexer;
    public RecDecParser(IPLPLexer lexer) {
        this.lexer = lexer;
    }
    private enum State { START, FUNCTION};
    @Override
    public void parse() throws Exception {
        IPLPToken token = lexer.nextToken();
        List<List<IPLPToken>> declarations = new ArrayList<>();
        List<IPLPToken> declaration = null;
        State state = State.START;
        while(token.getKind() != PLPTokenKinds.Kind.EOF){
            PLPTokenKinds.Kind tokenKind = token.getKind();
            declaration = new ArrayList<>();
            switch(tokenKind){
                case KW_FUN -> {
                    token = parseFunction(token, declaration);
                }
                case KW_VAL -> {}
                case KW_VAR -> {}
                default -> throw new SyntaxException("Declaration must start with FUN, VAR or VAL", token.getLine(), token.getCharPositionInLine());
            }

        }

    }

    private IPLPToken parseFunction(IPLPToken argToken, List<IPLPToken> declaration) throws SyntaxException, LexicalException {
        IPLPToken token = argToken;
        if(token.getKind() != PLPTokenKinds.Kind.KW_FUN){
            throw new SyntaxException("Expecting keyword 'FUN' ", token.getLine(),  token.getCharPositionInLine());
        }
        declaration.add(token);
        token = lexer.nextToken();
        if(token.getKind() != PLPTokenKinds.Kind.IDENTIFIER){
            throw new SyntaxException("Expecting token of type 'Identifier' ", token.getLine(),  token.getCharPositionInLine());
        }
        declaration.add(token);
        token = lexer.nextToken();
        if(token.getKind() != PLPTokenKinds.Kind.LPAREN){
            throw new SyntaxException("Expecting  Left paren '('", token.getLine(),  token.getCharPositionInLine());
        }
        declaration.add(token);
        token = lexer.nextToken();
        token = parseNameDef(token, declaration);
        while(token.getKind() == PLPTokenKinds.Kind.COMMA){
            declaration.add(token);
            token = lexer.nextToken();
            token = parseNameDef(token, declaration);
        }
        if(token.getKind() != PLPTokenKinds.Kind.RPAREN){
            throw new SyntaxException("Expecting close paren ')'or comma ','", token.getLine(),  token.getCharPositionInLine());
        }
        declaration.add(token);
        token = lexer.nextToken();
        token = parseFunctionReturnType(token, declaration);

        return token;
    }

    private IPLPToken parseFunctionReturnType(IPLPToken argToken, List<IPLPToken> declaration) throws SyntaxException, LexicalException {
        IPLPToken token = argToken;
        if(token.getKind() == PLPTokenKinds.Kind.KW_DO){
            return token;
        }
        if(token.getKind() != PLPTokenKinds.Kind.COLON){
            throw new SyntaxException("Expecting keyword 'DO 'or colon ':'", token.getLine(),  token.getCharPositionInLine());
        }
        declaration.add(token);
        token = lexer.nextToken();
        token = parseType(token, declaration);
        return token;
    }

    private IPLPToken parseType(IPLPToken argToken, List<IPLPToken> declaration) {
        return null;
    }

    private IPLPToken parseNameDef(IPLPToken argToken, List<IPLPToken> declaration) throws SyntaxException, LexicalException {
        IPLPToken token = argToken;
        if(token.getKind() == PLPTokenKinds.Kind.RPAREN){
            return token;
        }
        if(token.getKind() != PLPTokenKinds.Kind.IDENTIFIER){
            throw new SyntaxException("Expecting token of type 'Identifier' ", token.getLine(),  token.getCharPositionInLine());
        }
        declaration.add(token);
        token = lexer.nextToken();
        return token;
    }
}
