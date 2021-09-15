package edu.ufl.cise.plpfa21.assignment1;

import static edu.ufl.cise.plpfa21.assignment1.PLPTokenKinds.Kind;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class LexerTests{
    IPLPLexer getLexer(String input) {
        return CompilerComponentFactory.getLexer(input);
    }

    @Test
    public void testCompleteProgram() throws LexicalException {
        String input = """
                VAR a;
                VAR b = 123;
                INT while = 32;
                /*This is a multi
                line
                comment*/
                VAR someMeaningfulVariableName = 5;
                RETURN;""";


        IPLPLexer lexer = getLexer(input);
        IPLPToken[] expectedTokens = {
                new PLPToken(Kind.KW_VAR, "VAR", 1, 0, "VAR"),
                new PLPToken(Kind.IDENTIFIER, "a", 1, 4, "a"),
                new PLPToken(Kind.SEMI, ";", 1, 5, ";"),

                new PLPToken(Kind.KW_VAR, "VAR", 2, 0, "VAR"),
                new PLPToken(Kind.IDENTIFIER, "b", 2, 4, "b"),
                new PLPToken(Kind.ASSIGN, "=", 2, 6, "="),
                new PLPToken(Kind.INT_LITERAL, "123", 2, 8, 123),
                new PLPToken(Kind.SEMI, ";", 2, 11, ";"),

                new PLPToken(Kind.KW_INT, "INT", 3, 0, "INT"),
                new PLPToken(Kind.IDENTIFIER, "while", 3, 4, "while"),
                new PLPToken(Kind.ASSIGN, "=", 3, 10, "="),
                new PLPToken(Kind.INT_LITERAL, "32", 3, 12, 32),
                new PLPToken(Kind.SEMI, ";", 3, 14, ";"),

                new PLPToken(Kind.KW_VAR, "VAR", 7, 0, "VAR"),
                new PLPToken(Kind.IDENTIFIER, "someMeaningfulVariableName", 7, 4, "someMeaningfulVariableName"),
                new PLPToken(Kind.ASSIGN, "=", 7, 31, "="),
                new PLPToken(Kind.INT_LITERAL, "5", 7, 33, 5),
                new PLPToken(Kind.SEMI, ";", 7, 34, ";"),

                new PLPToken(Kind.KW_RETURN, "RETURN", 8, 0, "RETURN"),
                new PLPToken(Kind.SEMI, ";", 7, 34, ";")};

        for(int i = 0 ; i < 19; i++){
            IPLPToken expectedToken = expectedTokens[i];
            IPLPToken actualToken = lexer.nextToken();
            assertTokenEquals(expectedToken, actualToken);
        }
    }

    @Test
    public void simpleStringLiteralTest() throws LexicalException {
        String input = """
                VAR a = "abc";
                VAR x = 'xyz.';
                """;
        IPLPLexer lexer = getLexer(input);
        IPLPToken[] expectedTokens = {
                new PLPToken(Kind.KW_VAR, "VAR", 1, 0, "VAR"),
                new PLPToken(Kind.IDENTIFIER, "a", 1, 4, "a"),
                new PLPToken(Kind.ASSIGN, "=", 1, 6, "="),
                new PLPToken(Kind.STRING_LITERAL, "\"abc\"", 1, 8, "abc"),
                new PLPToken(Kind.SEMI, ";", 1, 13, ";"),

                new PLPToken(Kind.KW_VAR, "VAR", 2, 0, "VAR"),
                new PLPToken(Kind.IDENTIFIER, "x", 2, 4, "x"),
                new PLPToken(Kind.ASSIGN, "=", 2, 6, "="),
                new PLPToken(Kind.STRING_LITERAL, "'xyz.'", 2, 8, "xyz"),
                new PLPToken(Kind.SEMI, ";", 2, 14, ";")
        };

        for(int i = 0 ; i < 10; i++){
            IPLPToken expectedToken = expectedTokens[i];
            IPLPToken actualToken = lexer.nextToken();
            assertTokenEquals(expectedToken, actualToken);
        }
    }

    @Test
    public void stringLiteralTest() throws LexicalException {
        String input = """
                "abc"
                "new
                line"
                "	tab" a
                """;
        IPLPLexer lexer = getLexer(input);
        IPLPToken[] expectedTokens = {
                new PLPToken(Kind.STRING_LITERAL, "\"abc\"", 1, 0, "abc"),
                new PLPToken(Kind.STRING_LITERAL, "\"new\nline\"", 2, 0, "new\nline"),
                new PLPToken(Kind.STRING_LITERAL, "\"\ttab\"", 4, 0, "\ttab"),
                new PLPToken(Kind.IDENTIFIER, "a", 4, 7, "a")
        };

        for(int i = 0 ; i < 4; i++){
            IPLPToken expectedToken = expectedTokens[i];
            IPLPToken actualToken = lexer.nextToken();
            assertTokenEquals(expectedToken, actualToken);
        }
    }

    private void  assertTokenEquals(IPLPToken expectedToken, IPLPToken actualToken) {
        Assertions.assertEquals(expectedToken.getKind(), actualToken.getKind());
        Assertions.assertEquals(expectedToken.getLine(), actualToken.getLine());
        Assertions.assertEquals(expectedToken.getText(), actualToken.getText());
        Assertions.assertEquals(expectedToken.getCharPositionInLine(), actualToken.getCharPositionInLine());

        if(expectedToken.getKind() == Kind.INT_LITERAL){
            Assertions.assertEquals(expectedToken.getIntValue(), actualToken.getIntValue());
        }
        else{
            Assertions.assertEquals(expectedToken.getStringValue(), actualToken.getStringValue());
        }
    }
}
