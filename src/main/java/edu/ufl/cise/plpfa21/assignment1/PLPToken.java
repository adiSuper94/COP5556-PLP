package edu.ufl.cise.plpfa21.assignment1;

public class PLPToken implements IPLPToken{

    private final Kind kind;
    private final String text;
    private final int lineNumber;
    private final int charPosition;
    private int intVal;
    private String stringVal;


    public PLPToken(Kind kind, String text, int lineNumber, int charPosition, int intVal) {
        this.kind = kind;
        this.text = text;
        this.lineNumber = lineNumber;
        this.charPosition = charPosition;
        this.intVal = intVal;
    }

    public PLPToken(Kind kind, String text, int lineNumber, int charPosition, String stringVal) {
        this.kind = kind;
        this.text = text;
        this.lineNumber = lineNumber;
        this.charPosition = charPosition;
        this.stringVal = null;
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public int getLine() {
        return lineNumber;
    }

    @Override
    public int getCharPositionInLine() {
        return charPosition;
    }

    @Override
    public String getStringValue() {
        if(kind == Kind.STRING_LITERAL){
            if(stringVal == null){
                stringVal = text.substring(1, text.length() - 1);
                stringVal = stringVal.replace("\\b", "\b");
                stringVal = stringVal.replace("\\t", "\t");
                stringVal = stringVal.replace("\\n", "\n");
                stringVal = stringVal.replace("\\r", "\r");
                stringVal = stringVal.replace("\\f", "\f");
                stringVal = stringVal.replace("\\\"", "\"");
                stringVal = stringVal.replace("\\'", "'");
                stringVal = stringVal.replace("\\\\", "\\");
            }
            return stringVal;
        }
        return text;
    }

    @Override
    public int getIntValue() {
        if(kind == Kind.INT_LITERAL){
            return intVal;
        }
        return Integer.MIN_VALUE;
    }


}
