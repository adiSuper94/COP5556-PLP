package edu.ufl.cise.plpfa21.assignment1;

import java.util.ArrayList;
import java.util.List;

public class PLPLexer implements IPLPLexer{

    private final String input;
    private final char[] inputCharArr;
    private int charPtr = 0;
    private int lineNumber = 1, charPos = 1;
    public PLPLexer(String input) {
        this.input = input;
        inputCharArr = input.toCharArray();

    }

    @Override
    public IPLPToken nextToken() throws LexicalException {
        List<Character> rememberedChars = new ArrayList<>();
        PLPTokenKinds.Kind rememberedCharKind = null;
        while(charPtr < inputCharArr.length){
            char currChar = inputCharArr[charPtr];
            switch (currChar){
                case '=':
                    if (charPtr + 1 < inputCharArr.length && inputCharArr[charPtr+1] == '='){
                        charPtr += 2;
                        charPos += 2;
                        return new PLPToken(PLPTokenKinds.Kind.EQUALS, "==", lineNumber, charPos, "==");
                    }
                    else{
                        charPtr += 1;
                        charPos += 1;
                        return new PLPToken(PLPTokenKinds.Kind.ASSIGN, "=", lineNumber, charPos, "=");
                    }

                case '\n':
                    if (charPtr + 1 < inputCharArr.length && inputCharArr[charPtr+1] == '\r'){
                        charPtr += 1;
                    }
                    charPos = 1;
                    lineNumber++;
                    charPtr += 1;
                case '\t':
                    charPos += 4;
                    charPtr += 1;

                case ' ':
                    if(!rememberedChars.isEmpty()){
                        String text = rememberedChars.toString();
                        IPLPToken token = new PLPToken(rememberedCharKind, text, lineNumber, charPos, text);
                        rememberedCharKind = null;
                        rememberedChars.clear();
                        charPtr += 1;
                        return token;
                    }
                    charPtr += 1;

                default:
                    if (!rememberedChars.isEmpty() && rememberedCharKind == PLPTokenKinds.Kind.INT_LITERAL && Character.isDigit(currChar)){
                        rememberedChars.add(currChar);
                    }
                    else if (!rememberedChars.isEmpty()){
                        rememberedChars.add(currChar);
                    }
                    else if(Character.isAlphabetic(currChar)){
                        rememberedChars.add(currChar);
                        rememberedCharKind = PLPTokenKinds.Kind.IDENTIFIER;
                    }
                    else if(Character.isDigit(currChar)){
                        rememberedChars.add(currChar);
                        rememberedCharKind = PLPTokenKinds.Kind.INT_LITERAL;
                    }
                    charPtr += 1;
                    charPos += 1;
            }
        }
        return new PLPToken(PLPTokenKinds.Kind.EOF, "", lineNumber, charPos, "");
    }
}
