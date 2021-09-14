package edu.ufl.cise.plpfa21.assignment1;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DFALexer implements IPLPLexer{

    private final String input;
    private final char EOFChar = 0;
    private final char[] chars;
    private final ArrayList<IPLPToken> tokens;

    private int pos = 0;
    private int lineNum = 1, colNum = 0;

    private enum State { START, DIGITS, IDENTIFIER, DOUBLES}

    private final List<String> keywords = Arrays.asList("VAR", "VAL", "FUN", "DO", "END", "LET", "SWITCH", "CASE", "DEFAULT",
            "IF", "WHILE", "RETURN", "NIL", "TRUE", "FALSE", "INT", "STRING", "BOOLEAN", "LIST");

    private final Iterator<IPLPToken> tokenIterator;

    Map<Character, PLPTokenKinds.Kind> doubles = Stream.of(new Object[][] {
            { '=', PLPTokenKinds.Kind.EQUALS },
            { '&', PLPTokenKinds.Kind.AND },
            { '|', PLPTokenKinds.Kind.OR },
            { '!', PLPTokenKinds.Kind.NOT_EQUALS}
    }).collect(Collectors.toMap(data -> (Character) data[0], data -> (PLPTokenKinds.Kind) data[1]));

    Map<Character, PLPTokenKinds.Kind> singleSymbols = Stream.of(new Object[][] {
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
        tokens = new ArrayList<>();
        lex();
        tokenIterator = tokens.iterator();
    }

    private void lex(){
        State state = State.START;
        List<Character> processedChars = new ArrayList<>();
        while(pos < chars.length){
            char currChar = chars[pos];
            switch (state){
                case START -> {
                    processedChars.clear();
                    switch (currChar){
                        case '\n' -> { colNum = -1; lineNum ++; }
                        case '\t' -> {colNum += 3;}
                        case '\r' -> {
                            if(pos + 1 < chars.length && chars[pos+1] == '\n'){
                                lineNum ++;
                                colNum = -1;
                                pos++;
                            }
                        }
                        case ' ' -> { }
                        case '+', '-', '*', ',', ';', ':', '(', ')', '[', ']', '<', '>' -> {
                            tokens.add(new PLPToken(singleSymbols.get(currChar), String.valueOf(currChar), lineNum, colNum, String.valueOf(currChar)));
                        }
                        case '=', '&', '|', '!' -> {
                            state = State.DOUBLES;
                        }
                        case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                            state = State.DIGITS;
                        }
                        case EOFChar -> {
                            tokens.add(new PLPToken(PLPTokenKinds.Kind.EOF,"", lineNum, colNum, ""));
                            return;
                        }
                        default -> {
                            if(Character.isJavaIdentifierStart(currChar)){
                                state = State.IDENTIFIER;
                            }
                            else{
                                tokens.add(new PLPToken(PLPTokenKinds.Kind.ERROR, String.valueOf(currChar), lineNum, colNum, String.valueOf(currChar)));
                            }
                        }
                    }
                    processedChars.add(currChar);
                }

                case DIGITS -> {
                    switch (currChar){
                        case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                            processedChars.add(currChar);
                        }
                        default -> {
                            String intValString = processedChars.stream().map(String::valueOf).collect(Collectors.joining());
                            int intVal = Integer.parseInt(intValString);
                            tokens.add(new PLPToken(PLPTokenKinds.Kind.INT_LITERAL, String.valueOf(intVal), lineNum, colNum - intValString.length(), intVal));
                            pos--;
                            colNum--;
                            state = State.START;
                        }
                    }
                }

                case IDENTIFIER -> {
                    if(Character.isJavaIdentifierPart(currChar)){
                        processedChars.add(currChar);
                    }
                    else{
                        String id = processedChars.stream().map(String::valueOf).collect(Collectors.joining());
                        if(keywords.contains(id)){
                            // handle case for keywords
                            int a = 1;
                        }
                        else{
                            tokens.add(new PLPToken(PLPTokenKinds.Kind.IDENTIFIER, id, lineNum, colNum - id.length(), id));
                        }
                        pos--;
                        colNum--;
                        state = State.START;
                    }
                }

                case DOUBLES -> {
                    char processedChar = processedChars.get(0);
                    if(processedChar == '!'){
                        if(currChar == '='){
                            tokens.add(new PLPToken(doubles.get(currChar), "!=", lineNum, colNum - 1, "!="));
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
                        tokens.add(new PLPToken(PLPTokenKinds.Kind.ERROR, "", lineNum, colNum - 1, ""));
                    }
                    state = State.START;
                }
            }
            colNum++;
            pos++;
        }
    }
    @Override
    public IPLPToken nextToken() throws LexicalException {
        if(tokenIterator.hasNext()){
            IPLPToken token = tokenIterator.next();
            if(token.getKind() == PLPTokenKinds.Kind.ERROR){

                throw new LexicalException(token.getStringValue() + "is an invalid token", token.getLine(), token.getCharPositionInLine());
            }
            return token;
        }
        return null;
    }
}
