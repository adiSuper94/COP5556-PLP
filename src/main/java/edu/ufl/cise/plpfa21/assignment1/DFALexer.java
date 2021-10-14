package edu.ufl.cise.plpfa21.assignment1;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DFALexer implements IPLPLL1Lexer{

    private final String input;
    private final char EOFChar = 0;
    private final char[] chars;
    private final LinkedList<IPLPToken> tokens;

    private int pos = 0;
    private int lineNum = 1, colNum = 0;

    private enum State { START, DIGITS, IDENTIFIER, DOUBLES, MULTILINE_COMMENT, SINGLE_LINE_COMMENT,STRING_LITERAL };

    private final Map<String, PLPTokenKinds.Kind> keywords = Stream.of(new Object[][] {
            { "VAR", PLPTokenKinds.Kind.KW_VAR },
            { "VAL", PLPTokenKinds.Kind.KW_VAL },
            { "FUN", PLPTokenKinds.Kind.KW_FUN },
            { "DO", PLPTokenKinds.Kind.KW_DO },
            { "END", PLPTokenKinds.Kind.KW_END },
            { "LET", PLPTokenKinds.Kind.KW_LET },
            { "SWITCH", PLPTokenKinds.Kind.KW_SWITCH },
            { "CASE", PLPTokenKinds.Kind.KW_CASE },
            { "DEFAULT", PLPTokenKinds.Kind.KW_DEFAULT },
            { "IF", PLPTokenKinds.Kind.KW_IF },
            { "WHILE", PLPTokenKinds.Kind.KW_WHILE },
            { "RETURN", PLPTokenKinds.Kind.KW_RETURN },
            { "NIL", PLPTokenKinds.Kind.KW_NIL },
            { "TRUE", PLPTokenKinds.Kind.KW_TRUE },
            { "FALSE", PLPTokenKinds.Kind.KW_FALSE },
            { "INT", PLPTokenKinds.Kind.KW_INT },
            { "STRING", PLPTokenKinds.Kind.KW_STRING },
            { "BOOLEAN", PLPTokenKinds.Kind.KW_BOOLEAN },
            { "LIST", PLPTokenKinds.Kind.KW_LIST }

    }).collect(Collectors.toMap(data -> (String) data[0], data -> (PLPTokenKinds.Kind) data[1]));


    private final Map<Character, PLPTokenKinds.Kind> doubles = Stream.of(new Object[][] {
            { '=', PLPTokenKinds.Kind.EQUALS },
            { '&', PLPTokenKinds.Kind.AND },
            { '|', PLPTokenKinds.Kind.OR },
            { '!', PLPTokenKinds.Kind.NOT_EQUALS}
    }).collect(Collectors.toMap(data -> (Character) data[0], data -> (PLPTokenKinds.Kind) data[1]));

    private final Map<Character, PLPTokenKinds.Kind> singleSymbols = Stream.of(new Object[][] {
            { '+', PLPTokenKinds.Kind.PLUS },
            { '-', PLPTokenKinds.Kind.MINUS },
            { '*', PLPTokenKinds.Kind.TIMES },
            { ',', PLPTokenKinds.Kind.COMMA },
            { ':', PLPTokenKinds.Kind.COLON },
            { ';', PLPTokenKinds.Kind.SEMI },
            { '(', PLPTokenKinds.Kind.LPAREN },
            { ')', PLPTokenKinds.Kind.RPAREN },
            { '[', PLPTokenKinds.Kind.LSQUARE },
            { ']', PLPTokenKinds.Kind.RSQUARE },
            { '<', PLPTokenKinds.Kind.LT },
            { '>', PLPTokenKinds.Kind.GT }
    }).collect(Collectors.toMap(data -> (Character) data[0], data -> (PLPTokenKinds.Kind) data[1]));



    public DFALexer(String input) {
        this.input = input;
        chars = Arrays.copyOf(input.toCharArray(), input.length() + 1);
        chars[input.length()] = EOFChar;
        tokens = new LinkedList<>();
        lex();
    }

    private void lex(){
        State state = State.START;
        int tokenLineNum = -1, tokenColNum = -1;
        List<Character> processedChars = new ArrayList<>();
        while(pos < chars.length){
            char currChar = chars[pos];
            switch (state){
                case START -> {
                    processedChars.clear();
                    switch (currChar){
                        case '\n' -> { colNum = -1; lineNum ++; }
                        case '\t' -> {
                            /*int diff = colNum % 4;
                            if(diff == 0){
                                colNum += 3;
                            }
                            else{
                                colNum += diff - 1;
                            }*/
                        }
                        case '\r' -> {
                            if(pos + 1 < chars.length && chars[pos+1] == '\n'){
                                lineNum ++;
                                colNum = -1;
                                pos++;
                            }
                        }
                        case ' ' -> { }
                        case '+', '-', '*', ',', ';', ':', '(', ')', '[', ']', '<', '>' -> tokens.add(new PLPToken(singleSymbols.get(currChar), String.valueOf(currChar), lineNum, colNum, String.valueOf(currChar)));
                        case '=', '&', '|', '!' -> state = State.DOUBLES;
                        case '\'', '"' -> {
                            state = State.STRING_LITERAL;
                            tokenColNum = colNum;
                            tokenLineNum = lineNum;
                        }
                        case '/' ->{
                            if(pos + 1 < chars.length){
                                char nextChar = chars[pos+ 1];
                                if(nextChar == '*'){
                                    state = State.MULTILINE_COMMENT;
                                    pos++;
                                    colNum++;
                                }
                                /*
                                 * else if(nextChar == '/'){

                                    state = State.SINGLE_LINE_COMMENT;
                                    pos++;
                                    colNum++;

                                }*/
                                else{
                                    tokens.add(new PLPToken(PLPTokenKinds.Kind.DIV, "/", lineNum, colNum,"/"));
                                }
                            }

                        }
                        case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> state = State.DIGITS;
                        case EOFChar -> tokens.add(new PLPToken(PLPTokenKinds.Kind.EOF,"", lineNum, colNum, ""));

                        default -> {
                            if(Character.isJavaIdentifierStart(currChar)){
                                state = State.IDENTIFIER;
                            }
                            else{
                                tokens.add(new PLPToken(PLPTokenKinds.Kind.ERROR, "", lineNum, colNum, String.valueOf(currChar)));
                            }
                        }
                    }
                    processedChars.add(currChar);
                }

                case DIGITS -> {
                    switch (currChar){
                        case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> processedChars.add(currChar);
                        default -> {
                            String intValString = processedChars.stream().map(String::valueOf).collect(Collectors.joining());
                            try{
                                int intVal = Integer.parseInt(intValString);
                                tokens.add(new PLPToken(PLPTokenKinds.Kind.INT_LITERAL, String.valueOf(intVal), lineNum, colNum - intValString.length(), intVal));
                            }catch (NumberFormatException nfe){
                                tokens.add(new PLPToken(PLPTokenKinds.Kind.ERROR, "Invalid Number", lineNum, colNum - intValString.length(), intValString));
                            }

                            pos--;
                            colNum--;
                            state = State.START;
                        }
                    }
                }

                case IDENTIFIER -> {
                    if(Character.isJavaIdentifierPart(currChar) && currChar != 0){
                        processedChars.add(currChar);
                    }
                    else{
                        String id = processedChars.stream().map(String::valueOf).collect(Collectors.joining());
                        tokens.add(new PLPToken(keywords.getOrDefault(id, PLPTokenKinds.Kind.IDENTIFIER), id, lineNum, colNum - id.length(), id));
                        pos--;
                        colNum--;
                        state = State.START;
                    }
                }

                case DOUBLES -> {
                    char processedChar = processedChars.get(0);
                    if(processedChar == '!'){
                        if(currChar == '='){
                            tokens.add(new PLPToken(PLPTokenKinds.Kind.NOT_EQUALS, "!=", lineNum, colNum - 1, "!="));
                        }
                        else{
                            tokens.add(new PLPToken(PLPTokenKinds.Kind.BANG, "!", lineNum, colNum - 1, "!"));
                            pos--;
                            colNum--;
                        }

                    }
                    else if(processedChar == currChar){
                        String text = currChar + ""+ currChar;
                        tokens.add(new PLPToken(doubles.get(currChar), text, lineNum, colNum - 1, text));
                    }
                    else if (processedChar == '='){
                        tokens.add(new PLPToken(PLPTokenKinds.Kind.ASSIGN, "=", lineNum, colNum - 1, "="));
                        pos--;
                        colNum--;
                    }
                    else{
                        tokens.add(new PLPToken(PLPTokenKinds.Kind.ERROR, "", lineNum, colNum - 1, String.valueOf(processedChar)));
                        pos--;
                        colNum--;
                    }
                    state = State.START;
                }

                case MULTILINE_COMMENT -> {
                    switch (currChar){
                        case '\n' -> { colNum = -1; lineNum ++; }
                        case '\t' -> {
                            /*int diff = colNum % 4;
                            if(diff == 0){
                                colNum += 3;
                            }
                            else{
                                colNum += diff - 1;
                            }*/
                        }
                        case '\r' -> {
                            if(pos + 1 < chars.length && chars[pos+1] == '\n'){
                                lineNum ++;
                                colNum = -1;
                                pos++;
                            }
                        }
                        case '*' -> {
                            if(pos + 1 < chars.length && chars[pos+1] == '/'){
                                state = State.START;
                                pos++;
                                colNum++;
                            }
                            else{
                                tokens.add(new PLPToken(PLPTokenKinds.Kind.ERROR, "'*' isn't allowed in a comment", lineNum, colNum - 1, "*"));
                            }
                        }
                    }
                }

                case STRING_LITERAL -> {
                    final char quote = processedChars.get(0);
                    switch(currChar){
                        case'\\' -> {
                            if(pos + 1 < chars.length){
                                char nextChar = chars[pos + 1];
                                switch(nextChar){
                                    case 'b', 't', 'n', 'r', 'f', '"', '\'', '\\'-> {
                                        processedChars.add(currChar);
                                        processedChars.add(nextChar);
                                        pos++;
                                        colNum++;
                                    }
                                    default -> {
                                        tokens.add(new PLPToken(PLPTokenKinds.Kind.ERROR, currChar + nextChar+ ": this char seq is not allowed in a string literal", lineNum, colNum, currChar + "" + nextChar));
                                        state = State.START;
                                    }
                                }
                            }
                            else{
                                tokens.add(new PLPToken(PLPTokenKinds.Kind.ERROR, currChar + ": this char seq is not allowed in a string literal", lineNum, colNum, currChar));
                                state = State.START;
                            }
                        }
                        case '\n' -> { colNum = -1; lineNum ++;}
                        case '\t' -> {
                            /*int diff = colNum % 4;
                            if(diff == 0){
                                colNum += 3;
                            }
                            else{
                                colNum += diff - 1;
                            }*/
                        }
                        case '\r' -> {
                            if(pos + 1 < chars.length && chars[pos+1] == '\n'){
                                lineNum ++;
                                colNum = -1;
                                pos++;
                            }
                        }
                        default -> {
                            if(currChar == quote){
                                String stringLiteral = processedChars.stream().map(String::valueOf).collect(Collectors.joining()) + quote;
                                tokens.add(new PLPToken(PLPTokenKinds.Kind.STRING_LITERAL, stringLiteral, tokenLineNum, tokenColNum, stringLiteral));
                                state = State.START;
                            }
                        }
                    }
                    processedChars.add(currChar);
                }
            }
            colNum++;
            pos++;
        }
    }

    @Override
    public IPLPToken nextToken() throws LexicalException {
        if(tokens.peek() != null){
            IPLPToken token = tokens.pop();
            if(token.getKind() == PLPTokenKinds.Kind.ERROR){
                if(token.getText().isBlank()){
                    throw new LexicalException(token.getStringValue() + "is an invalid token", token.getLine(), token.getCharPositionInLine());
                }
                throw new LexicalException(token.getText()+ ":" + token.getStringValue(), token.getLine(), token.getCharPositionInLine());
            }
            return token;
        }
        return null;
    }

    @Override
    public IPLPToken peekNextToken(){
        if(tokens.peek() != null){
            IPLPToken token = tokens.peek();
            if(token.getKind() == PLPTokenKinds.Kind.ERROR){
                return null;
            }
            return token;
        }
        return null;
    }
}
