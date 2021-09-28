package edu.ufl.cise.plpfa21.assignment2;

import edu.ufl.cise.plpfa21.assignment1.IPLPLexer;
import edu.ufl.cise.plpfa21.assignment1.IPLPToken;
import edu.ufl.cise.plpfa21.assignment1.LexicalException;
import edu.ufl.cise.plpfa21.assignment1.PLPTokenKinds.Kind;

import java.util.ArrayList;
import java.util.Arrays;
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
        while(token.getKind() != Kind.EOF){
            Kind tokenKind = token.getKind();
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
        if(token.getKind() != Kind.KW_FUN){
            throw new SyntaxException("Expecting keyword 'FUN' ", token.getLine(),  token.getCharPositionInLine());
        }
        declaration.add(token);

        token = lexer.nextToken();
        if(token.getKind() != Kind.IDENTIFIER){
            throw new SyntaxException("Expecting token of type 'Identifier' ", token.getLine(),  token.getCharPositionInLine());
        }
        declaration.add(token);

        token = lexer.nextToken();
        if(token.getKind() != Kind.LPAREN){
            throw new SyntaxException("Expecting  Left paren '('", token.getLine(),  token.getCharPositionInLine());
        }
        declaration.add(token);

        token = lexer.nextToken();
        token = parseNameDef(token, declaration);
        while(token.getKind() == Kind.COMMA){
            declaration.add(token);
            token = lexer.nextToken();
            token = parseNameDef(token, declaration);
        }
        if(token.getKind() != Kind.RPAREN){
            throw new SyntaxException("Expecting close paren ')'or comma ','", token.getLine(),  token.getCharPositionInLine());
        }
        declaration.add(token);

        token = lexer.nextToken();
        token = parseFunctionReturnType(token, declaration);
        if(token.getKind() != Kind.KW_DO){
            throw new SyntaxException("Expecting keyword 'DO' ", token.getLine(),  token.getCharPositionInLine());
        }
        declaration.add(token);

        token = parseBlock(token, declaration);
        if(token.getKind() != Kind.KW_END){
            throw new SyntaxException("Expecting keyword 'END' ", token.getLine(),  token.getCharPositionInLine());
        }
        declaration.add(token);

        token = lexer.nextToken();
        return token;
    }

    private IPLPToken parseBlock(IPLPToken argToken, List<IPLPToken> declaration) {
        return null;
    }

    private IPLPToken parseFunctionReturnType(IPLPToken argToken, List<IPLPToken> declaration) throws SyntaxException, LexicalException {
        IPLPToken token = argToken;
        if(token.getKind() == Kind.KW_DO){
            return token;
        }
        if(token.getKind() != Kind.COLON){
            throw new SyntaxException("Expecting keyword 'DO 'or colon ':'", token.getLine(),  token.getCharPositionInLine());
        }
        declaration.add(token);

        token = lexer.nextToken();
        token = parseType(token, declaration);
        return token;
    }

    private IPLPToken parseType(IPLPToken argToken, List<IPLPToken> declaration) throws LexicalException, SyntaxException {
        IPLPToken token = argToken;

        if(token.getKind() == Kind.RSQUARE){
            return token;
        }
        List<Kind> validPrimitives = Arrays.asList(Kind.KW_INT, Kind.KW_STRING, Kind.KW_BOOLEAN);
        if(validPrimitives.contains(token.getKind())){
            declaration.add(token);
            token = lexer.nextToken();
            return token;
        }

        if(token.getKind() != Kind.KW_LIST){
            throw new SyntaxException("Expecting keyword 'List 'or primitives 'INT, BOOLEAN, STRING'", token.getLine(),  token.getCharPositionInLine());
        }
        declaration.add(token);

        token = lexer.nextToken();
        if (token.getKind() != Kind.LSQUARE){
            throw new SyntaxException("Expecting keyword Left square paren '['", token.getLine(),  token.getCharPositionInLine());
        }
        declaration.add(token);

        token = lexer.nextToken();
        token = parseType(token, declaration);

        if (token.getKind() != Kind.RSQUARE){
            throw new SyntaxException("Expecting keyword Right square paren ']'", token.getLine(),  token.getCharPositionInLine());
        }
        declaration.add(token);
        token = lexer.nextToken();
        return token;
    }

    private IPLPToken parseNameDef(IPLPToken argToken, List<IPLPToken> declaration) throws SyntaxException, LexicalException {
        IPLPToken token = argToken;
        if(token.getKind() == Kind.RPAREN){
            return token;
        }
        if(token.getKind() != Kind.IDENTIFIER){
            throw new SyntaxException("Expecting token of type 'Identifier' ", token.getLine(),  token.getCharPositionInLine());
        }
        declaration.add(token);
        token = lexer.nextToken();
        return token;
    }
}
