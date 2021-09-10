package edu.ufl.cise.plpfa21.assignment1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PLPLexer implements IPLPLexer{

    private final String input;
    private final char[] inputCharArr;
    private int charPtr = 0;
    private int lineNumber = 1, charPos = 0;

    List<Character> rememberedChars = new ArrayList<>();
    private int rememberedCharLineNumber, rememberedCharCharPos;
    PLPTokenKinds.Kind rememberedCharKind = null;
    public PLPLexer(String input) {
        this.input = input;
        inputCharArr = input.toCharArray();

    }

    @Override
    public IPLPToken nextToken() throws LexicalException {
        while(charPtr < inputCharArr.length){
            char currChar = inputCharArr[charPtr];
            switch (currChar){
                case '=' ->{
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
                }

                case '\n' ->{
                    if (charPtr + 1 < inputCharArr.length && inputCharArr[charPtr+1] == '\r'){
                        charPtr += 1;
                    }
                    charPos = 0;
                    lineNumber++;
                    if(!rememberedChars.isEmpty()){
                        return getBufferToken();
                    }
                    charPtr += 1;
                }

                case '\t' ->{
                    charPos += 4;
                    if(!rememberedChars.isEmpty()){
                        return getBufferToken();
                    }
                    charPtr += 1;
                }

                case ' ' ->{
                    if(!rememberedChars.isEmpty()){
                        return getBufferToken();
                    }
                    charPtr += 1;
                    charPos += 1;
                }


                default ->{
                    if (!rememberedChars.isEmpty() && rememberedCharKind == PLPTokenKinds.Kind.INT_LITERAL && Character.isDigit(currChar)){
                        rememberedChars.add(currChar);
                    }
                    else if (!rememberedChars.isEmpty()){
                        rememberedChars.add(currChar);
                    }
                    else if(Character.isAlphabetic(currChar)){
                        rememberedChars.add(currChar);
                        rememberedCharKind = PLPTokenKinds.Kind.IDENTIFIER;
                        rememberedCharLineNumber = lineNumber;
                        rememberedCharCharPos = charPos;

                    }
                    else if(Character.isDigit(currChar)){
                        rememberedChars.add(currChar);
                        rememberedCharKind = PLPTokenKinds.Kind.INT_LITERAL;
                        rememberedCharLineNumber = lineNumber;
                        rememberedCharCharPos = charPos;
                    }
                    charPtr += 1;
                    charPos += 1;
                }

            }
        }
        return new PLPToken(PLPTokenKinds.Kind.EOF, "", lineNumber, charPos, "");
    }

    private IPLPToken getBufferToken(){
        String text = rememberedChars.stream().map(String::valueOf).collect(Collectors.joining());
        IPLPToken token = new PLPToken(rememberedCharKind, text, rememberedCharLineNumber, rememberedCharCharPos, text);
        rememberedCharKind = null;
        rememberedChars.clear();
        charPtr += 1;
        return token;
    }
}
