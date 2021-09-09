package edu.ufl.cise.plpfa21.assignment1;

public class PLPToken implements IPLPToken{

    private Kind kind;
    private String text;
    private int lineNumber;
    private int charPosition;
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
        this.stringVal = stringVal;
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
        return stringVal;
    }

    @Override
    public int getIntValue() {
        return intVal;
    }

    public void setIntVal(int intVal) {
        this.intVal = intVal;
    }

    public void setStringVal(String stringVal) {
        this.stringVal = stringVal;
    }
}
