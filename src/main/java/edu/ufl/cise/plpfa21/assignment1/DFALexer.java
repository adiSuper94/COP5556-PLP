package edu.ufl.cise.plpfa21.assignment1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class DFALexer implements IPLPLexer{

    private final String input;
    private final char EOFChar = 0;
    private final char[] chars;
    private final ArrayList<IPLPToken> tokens;

    private int pos = 0;
    private int lineNum = 1, colNum = 0;

    private enum State { START, DIGITS, IDENTIFIER}

    private final List<String> keywords = Arrays.asList("VAR", "VAL", "FUN", "DO", "END", "LET", "SWITCH", "CASE", "DEFAULT",
            "IF", "WHILE", "RETURN", "NIL", "TRUE", "FALSE", "INT", "STRING", "BOOLEAN", "LIST");

    private final Iterator<IPLPToken> tokenIterator;

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
                        case '\t' -> {}
                        case '\r' -> {}
                        case ' ' -> {}
                        case '+' -> {}
                        case '-' -> {}
                        case '*' -> {}
                        case ',' -> {}
                        case ';' -> {}
                        case ':' -> {}
                        case ']' -> {}
                        case '[' -> {}
                        case '(' -> {}
                        case ')' -> {}
                        case '>' -> {}
                        case '<' -> {}
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
                        }
                    }
                    colNum++;
                    processedChars.add(currChar);
                }
                case DIGITS -> {
                    switch (currChar){
                        case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                            processedChars.add(currChar);
                            colNum++;
                        }
                        default -> {
                            String intValString = processedChars.stream().map(String::valueOf).collect(Collectors.joining());
                            int intVal = Integer.parseInt(intValString);
                            tokens.add(new PLPToken(PLPTokenKinds.Kind.INT_LITERAL, String.valueOf(intVal), lineNum, colNum - intValString.length(), intVal));
                            pos--;
                            state = State.START;
                        }
                    }
                }

                case IDENTIFIER -> {
                    if(Character.isJavaIdentifierPart(currChar)){
                        processedChars.add(currChar);
                        colNum++;
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
                        state = State.START;
                    }
                }
            }
            pos++;
        }
    }
    @Override
    public IPLPToken nextToken() throws LexicalException {
        if(tokenIterator.hasNext()){
            IPLPToken token = tokenIterator.next();
            if(token.getKind() == PLPTokenKinds.Kind.ERROR){
                throw new LexicalException(token.getStringValue(), token.getLine(), token.getCharPositionInLine());
            }
            return token;
        }
        return null;
    }
}
