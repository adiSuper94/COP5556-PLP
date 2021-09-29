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
    private enum State { START}
    @Override
    public void parse() throws Exception {
        IPLPToken token = lexer.nextToken();
        List<List<IPLPToken>> declarations = new ArrayList<>();
        State state = State.START;
        List<IPLPToken> declaration;
        while(Kind.EOF != token.getKind()){
            Kind tokenKind = token.getKind();
            declaration = new ArrayList<>();
            switch(tokenKind){
                case KW_FUN -> {
                    token = parseFunction(token, declaration);
                    declarations.add(declaration);
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

    private IPLPToken parseStatement(IPLPToken argToken, List<IPLPToken> declaration) throws LexicalException, SyntaxException {
        IPLPToken token = argToken;
        switch (token.getKind()){
            case KW_LET -> {
                declaration.add(token);

                token = lexer.nextToken();
                token = parseNameDef(token, declaration);
                if(token.getKind() == Kind.ASSIGN){
                    declaration.add(token);
                    token = lexer.nextToken();
                    token = parseExpression(token, declaration);
                }
                if(token.getKind() != Kind.SEMI){
                    throw new SyntaxException("Expecting semicolon ", token.getLine(),  token.getCharPositionInLine());
                }
                declaration.add(token);

                token = lexer.nextToken();
                return token;
            }
            case KW_SWITCH -> {}
            case KW_IF -> {}
            case KW_WHILE -> {}
            case KW_RETURN -> {}
            default -> {
                // dafaq
            }
        }

        return null;
    }

    private IPLPToken parseExpression(IPLPToken token, List<IPLPToken> declaration) throws LexicalException, SyntaxException {
        return parseLogicalExpression(token, declaration);
    }

    private IPLPToken parseLogicalExpression(IPLPToken argToken, List<IPLPToken> declaration) throws LexicalException, SyntaxException {
        IPLPToken token = argToken;
        token = parseComparisonExpression(token, declaration);
        if(token.getKind() == Kind.EQUALS || token.getKind() == Kind.OR){
            declaration.add(token);

            token = lexer.nextToken();
            token = parseComparisonExpression(token, declaration);
            return token;
        }

        return token;
    }

    private IPLPToken parseComparisonExpression(IPLPToken argToken, List<IPLPToken> declaration) throws LexicalException, SyntaxException {
        IPLPToken token = argToken;
        token = parseAdditiveExpression(token, declaration);
        List<Kind> validSymbols = Arrays.asList(Kind.LT, Kind.GT, Kind.EQUALS, Kind.NOT_EQUALS);
        if(validSymbols.contains(token.getKind())){
            declaration.add(token);

            token = lexer.nextToken();
            token = parseAdditiveExpression(token, declaration);
            return token;
        }
        return token;
    }

    private IPLPToken parseAdditiveExpression(IPLPToken argToken, List<IPLPToken> declaration) throws LexicalException, SyntaxException {
        IPLPToken token = argToken;
        token = parseMultiplicativeExpression(token, declaration);
        List<Kind> validSymbols = Arrays.asList(Kind.PLUS, Kind.MINUS);
        if(validSymbols.contains(token.getKind())){
            declaration.add(token);

            token = lexer.nextToken();
            token = parseMultiplicativeExpression(token, declaration);
            return token;
        }
        return token;
    }

    private IPLPToken parseMultiplicativeExpression(IPLPToken argToken, List<IPLPToken> declaration) throws LexicalException, SyntaxException {
        IPLPToken token = argToken;
        token = parseUnaryExpression(token, declaration);
        List<Kind> validSymbols = Arrays.asList(Kind.TIMES, Kind.DIV);
        if(validSymbols.contains(token.getKind())){
            declaration.add(token);

            token = lexer.nextToken();
            token = parseUnaryExpression(token, declaration);
            return token;
        }
        return token;
    }

    private IPLPToken parseUnaryExpression(IPLPToken argToken, List<IPLPToken> declaration) throws LexicalException, SyntaxException {
        IPLPToken token = argToken;
        List<Kind> validSymbols = Arrays.asList(Kind.BANG, Kind.MINUS);
        if(validSymbols.contains(token.getKind())){
            declaration.add(token);

            token = lexer.nextToken();
            
        }
        token = parsePrimaryExpression(token, declaration);
        return token;
    }

    private IPLPToken parsePrimaryExpression(IPLPToken  token, List<IPLPToken> declaration) throws LexicalException, SyntaxException {
        List<Kind> validPrimaryExp = Arrays.asList(Kind.KW_NIL, Kind.KW_TRUE, Kind.KW_FALSE, Kind.STRING_LITERAL, Kind.INT_LITERAL);
        if(validPrimaryExp.contains(token.getKind())){
            declaration.add(token);
            token = lexer.nextToken();
            return token;
        }
        if(token.getKind() == Kind.LPAREN){
            declaration.add(token);
            token = parseExpression(token, declaration);
            if (token.getKind() != Kind.RPAREN){
                throw new SyntaxException("Expecting Right paren '('", token.getLine(),  token.getCharPositionInLine());
            }
        }
        if(token.getKind() == Kind.IDENTIFIER){
            declaration.add(token);

            token = lexer.nextToken();
            if(token.getKind() == Kind.LPAREN){
                declaration.add(token);

                token = lexer.nextToken();
                if(token.getKind() != Kind.RPAREN) {
                    token = parseExpression(token, declaration);
                    while (token.getKind() == Kind.COMMA) {
                        declaration.add(token);
                        token = lexer.nextToken();
                        if(token.getKind() != Kind.RPAREN){
                            token = parseExpression(token, declaration);
                        }
                    }
                    if (token.getKind() != Kind.RPAREN) {
                        throw new SyntaxException("Expecting close paren ')'or comma ','", token.getLine(), token.getCharPositionInLine());
                    }
                    declaration.add(token);

                    token = lexer.nextToken();
                }
                declaration.add(token);
                token = lexer.nextToken();
                return token;
            }
            else if(token.getKind() == Kind.LSQUARE){
                declaration.add(token);

                token = lexer.nextToken();
                token = parseExpression(token, declaration);
                if(token.getKind() != Kind.RSQUARE){
                    throw new SyntaxException("Expecting close sq paren ']'", token.getLine(), token.getCharPositionInLine());
                }
                declaration.add(token);
                token = lexer.nextToken();
                return token;
            }
        }
        throw new SyntaxException("Expecting NIL | TRUE | FALSE |  IntLiteral | StringLiteral   |  ( Expression ) |\n" +
                "    Identifier  ( (Expression ( , Expression)* )? )  |\n" +
                "    Identifier |  Identifier [ Expression ]    \n", token.getLine(), token.getCharPositionInLine());
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
