package edu.ufl.cise.plpfa21.assignment2;

import edu.ufl.cise.plpfa21.assignment1.*;
import edu.ufl.cise.plpfa21.assignment1.PLPTokenKinds.Kind;
import edu.ufl.cise.plpfa21.assignment3.ast.*;
import edu.ufl.cise.plpfa21.assignment3.astimpl.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecDecParser implements IPLPParser{
    IPLPLL1Lexer lexer;
    public RecDecParser(IPLPLL1Lexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public IASTNode parse() throws Exception {
        IPLPToken token = lexer.nextToken();
        List<IDeclaration> program = new ArrayList<>();

        IDeclaration declaration;
        while(Kind.EOF != token.getKind()){
            Kind tokenKind = token.getKind();

            switch(tokenKind){
                case KW_FUN -> {
                    declaration = parseFunction(token);
                    program.add(declaration);
                    token =  lexer.nextToken();
                }
                case KW_VAL -> {
                    declaration = parseValDeclaration(token);
                    program.add(declaration);
                    token =  lexer.nextToken();
                }
                case KW_VAR -> {
                    declaration = parseVarDeclaration(token);
                    program.add(declaration);
                    token =  lexer.nextToken();
                }
                default -> throw new SyntaxException("Declaration must start with FUN, VAR or VAL", token.getLine(), token.getCharPositionInLine());
            }

        }

        return new Program__(0, 0, "", program);
    }

    private IMutableGlobal parseVarDeclaration(IPLPToken token) throws SyntaxException, LexicalException {
        int line = token.getLine();
        int posInLine = token.getCharPositionInLine();
        String text = token.getText();
        if(token.getKind() != Kind.KW_VAR){
            throw new SyntaxException("Expecting keyword 'VAR' ", token.getLine(),  token.getCharPositionInLine());
        }

        INameDef varName = parseNameDef(lexer.nextToken());
        token =  lexer.nextToken();
        IExpression expression= parseOptionalExpAssignment(token);
        return new MutableGlobal__(line, posInLine, text, varName, expression);
    }

    private IExpression parseOptionalExpAssignment(IPLPToken token) throws LexicalException, SyntaxException {

        IExpression exp = null;
        if(token.getKind() == Kind.ASSIGN){
            exp = parseExpression(lexer.nextToken());
            token = lexer.nextToken();
        }


        if(token.getKind() != Kind.SEMI){
            throw new SyntaxException("Expecting declaration delimiter ';' ", token.getLine(),  token.getCharPositionInLine());
        }
        return exp;
    }

    private ImmutableGlobal__ parseValDeclaration(IPLPToken token) throws SyntaxException, LexicalException {
        int line = token.getLine();
        int posInLine = token.getCharPositionInLine();
        String text = token.getText();
        if(token.getKind() != Kind.KW_VAL){
            throw new SyntaxException("Expecting keyword 'VAL' ", token.getLine(),  token.getCharPositionInLine());
        }

        INameDef valName = parseNameDef(lexer.nextToken());
        token = lexer.nextToken();
        if(token.getKind() != Kind.ASSIGN){
            throw new SyntaxException("Expecting assignment operator '=' ", token.getLine(),  token.getCharPositionInLine());
        }

        IExpression expression = parseExpression(lexer.nextToken());
        token = lexer.nextToken();
        if(token.getKind() != Kind.SEMI){
            throw new SyntaxException("Expecting declaration delimiter ';' ", token.getLine(),  token.getCharPositionInLine());
        }
        return new ImmutableGlobal__(line, posInLine, text, valName, expression);
    }

    private IFunctionDeclaration parseFunction(IPLPToken token) throws SyntaxException, LexicalException {
        IFunctionDeclaration funcDec;

        IIdentifier name;
        List<INameDef> args = new ArrayList<>();
        IType returnType;
        IBlock body;

        int line, posInLine;
        String text;

        if(token.getKind() != Kind.KW_FUN){
            throw new SyntaxException("Expecting keyword 'FUN' ", token.getLine(),  token.getCharPositionInLine());
        }
        line = token.getLine();
        posInLine = token.getCharPositionInLine();
        text = token.getText();
        token = lexer.nextToken();

        if(token.getKind() != Kind.IDENTIFIER){
            throw new SyntaxException("Expecting token of type 'Identifier' ", token.getLine(),  token.getCharPositionInLine());
        }
        name = new Identifier__(token.getLine(), token.getCharPositionInLine(), token.getText(), token.getText());

        token = lexer.nextToken();
        if(token.getKind() != Kind.LPAREN){
            throw new SyntaxException("Expecting  Left paren '('", token.getLine(),  token.getCharPositionInLine());
        }

        token = lexer.nextToken();
        if(token.getKind() != Kind.RPAREN){
            args.add(parseNameDef(token));
            token = lexer.nextToken();
            while(token.getKind() == Kind.COMMA){
                args.add(parseNameDef(lexer.nextToken()));
                token = lexer.nextToken();
            }
        }
        if(token.getKind() != Kind.RPAREN){
            throw new SyntaxException("Expecting close paren ')'or comma ','", token.getLine(),  token.getCharPositionInLine());
        }

        token = lexer.nextToken();
        returnType = parseFunctionReturnType(token);
        body = parseDoBlock(token);
        funcDec = new FunctionDeclaration___(line, posInLine, text, name, args, returnType, body);
        return funcDec;
    }

    private IBlock parseDoBlock(IPLPToken token) throws LexicalException, SyntaxException {
        IBlock block;

        List<IStatement> statements = new ArrayList<>();
        int line, posInLine;
        String text;

        if(token.getKind() != Kind.KW_DO){
            throw new SyntaxException("Expecting keyword 'DO' ", token.getLine(),  token.getCharPositionInLine());
        }
        line = token.getLine();
        posInLine = token.getCharPositionInLine();
        text = token.getText();

        IPLPToken nextToken = lexer.peekNextToken();
        while(nextToken.getKind() != Kind.KW_END){
            IStatement statement = parseStatement(nextToken);
            if(statement != null){
                statements.add(statement);
            }
            nextToken = lexer.nextToken();
        }
        block = new Block__(line, posInLine, text, statements);

        //token = lexer.nextToken();
        return block;
    }

    private IStatement parseStatement(IPLPToken nextToken) throws LexicalException, SyntaxException {
        if(nextToken.getKind() == Kind.KW_END){
            return null;
        }
        IPLPToken token = lexer.nextToken();
        int line = token.getLine();
        int posInLine = token.getCharPositionInLine();
        String text = token.getText();
        switch (token.getKind()){
            case KW_LET -> {
                IBlock block =  null;
                IExpression expression;
                INameDef name;
                name = parseNameDef(lexer.nextToken());
                expression = parseOptionalExpAssignment(lexer.nextToken());
                if(expression != null){
                    token = lexer.nextToken();
                }
                block = parseDoBlock(token);
                return new LetStatement__(line, posInLine, text, block, expression, name);
            }
            case KW_SWITCH -> {
                IExpression switchExp = parseExpression(lexer.nextToken());
                List<IBlock> blocks = new ArrayList<>();
                List<IExpression> branchExpressions = new ArrayList<>();
                parseOptionalCases(lexer.peekNextToken(), branchExpressions, blocks);
                token = lexer.nextToken();
                if(token.getKind() != Kind.KW_DEFAULT){
                    throw new SyntaxException("Expecting keyword 'DEFAULT' for switch ", token.getLine(),  token.getCharPositionInLine());
                }

                IBlock defaultBlock = parseBlock(lexer.peekNextToken(), new Block__(token.getLine(), token.getCharPositionInLine(), token.getText(), new ArrayList<>()));
                token = lexer.nextToken();
                if(token.getKind() != Kind.KW_END){
                    throw new SyntaxException("Expecting keyword 'END' for switch ", token.getLine(),  token.getCharPositionInLine());
                }
                return new SwitchStatement__(line, posInLine, text, switchExp, branchExpressions, blocks, defaultBlock);
            }
            case KW_IF -> {
                IExpression exp = parseExpression(lexer.nextToken());
                IBlock block = parseDoBlock(lexer.nextToken());
                return new IfStatement__(line, posInLine, text, exp, block);
            }
            case  KW_WHILE -> {
                IExpression exp = parseExpression(lexer.nextToken());
                IBlock block = parseDoBlock(lexer.nextToken());
                return new WhileStatement__(line, posInLine, text, exp, block);
            }
            case KW_RETURN -> {
                IExpression exp = parseExpression(lexer.nextToken());
                token = lexer.nextToken();
                if(token.getKind() != Kind.SEMI){
                    throw new SyntaxException("Expecting semicolon ';' ", token.getLine(),  token.getCharPositionInLine());
                }
                return new ReturnStatement__(line, posInLine, text, exp);
            }
            default -> {
                IExpression leftExp = parseExpression(token);
                IExpression rightExp= parseOptionalExpAssignment(lexer.nextToken());
                return new AssignmentStatement__(line, posInLine, text, leftExp, rightExp);
            }
        }
    }

    private IBlock parseBlock(IPLPToken nextToken, IBlock block) throws LexicalException, SyntaxException {
        if(nextToken.getKind() == Kind.KW_END){
            return block;
        }

        IStatement statement = parseStatement(lexer.peekNextToken());
        if(statement != null){
            block.getStatements().add(statement);
        }
        return parseBlock(lexer.peekNextToken(), block);
    }

    private IBlock parseCaseBlock(IPLPToken nextToken, IBlock block) throws LexicalException, SyntaxException {
        if(nextToken.getKind() == Kind.KW_DEFAULT || nextToken.getKind() == Kind.KW_CASE){
            return block;
        }
        IStatement statement = parseStatement(lexer.peekNextToken());
        if(statement != null){
            block.getStatements().add(statement);
        }
        return parseCaseBlock(lexer.peekNextToken(), block);
    }

    private void parseOptionalCases(IPLPToken nextToken, List<IExpression> expressions, List<IBlock> blocks) throws SyntaxException, LexicalException {
        if(nextToken.getKind() == Kind.KW_DEFAULT){
            return;
        }
        IPLPToken token = lexer.nextToken();
        if(token.getKind() != Kind.KW_CASE){
            throw new SyntaxException("Expecting keyword 'CASE' for switch ", token.getLine(),  token.getCharPositionInLine());
        }

        IExpression expression = parseExpression(lexer.nextToken());
        expressions.add(expression);
        token = lexer.nextToken();
        if(token.getKind() != Kind.COLON){
            throw new SyntaxException("Expecting :  for case ", token.getLine(),  token.getCharPositionInLine());
        }

        IBlock caseBlock = parseCaseBlock(lexer.peekNextToken(), new Block__(token.getLine(), token.getCharPositionInLine(), token.getText(), new ArrayList<>()));
        blocks.add(caseBlock);
        parseOptionalCases(lexer.peekNextToken(), expressions, blocks);
    }

    private IExpression parseExpression(IPLPToken token) throws LexicalException, SyntaxException {
        return parseLogicalExpression(token);
    }

    private IExpression parseLogicalExpression(IPLPToken token) throws LexicalException, SyntaxException {
        int line = token.getLine();
        int posInLine = token.getCharPositionInLine();
        String text = token.getText();
        IExpression leftExpression= parseComparisonExpression(token);
        IPLPToken nextToken = lexer.peekNextToken();
        if(nextToken.getKind() == Kind.AND || nextToken.getKind() == Kind.OR){
            IPLPToken opToken = lexer.nextToken();
            token = lexer.nextToken();
            IExpression rightExpression = parseComparisonExpression(token);
            return new BinaryExpression__(line, posInLine, text, leftExpression, rightExpression, opToken.getKind());
        }
        return leftExpression;
    }

    private IExpression parseComparisonExpression(IPLPToken token) throws LexicalException, SyntaxException {
        int line = token.getLine();
        int posInLine = token.getCharPositionInLine();
        String text = token.getText();
        IExpression leftExpression = parseAdditiveExpression(token);
        List<Kind> validSymbols = Arrays.asList(Kind.LT, Kind.GT, Kind.EQUALS, Kind.NOT_EQUALS);
        IPLPToken nextToken = lexer.peekNextToken();
        if(validSymbols.contains(nextToken.getKind())){
            IPLPToken opToken = lexer.nextToken();
            token = lexer.nextToken();
            IExpression rightExpression = parseAdditiveExpression(token);
            return new BinaryExpression__(line, posInLine, text, leftExpression, rightExpression, opToken.getKind());
        }
        return leftExpression;
    }

    private IExpression parseAdditiveExpression(IPLPToken token) throws LexicalException, SyntaxException {
        int line = token.getLine();
        int posInLine = token.getCharPositionInLine();
        String text = token.getText();
        IExpression leftExpression = parseMultiplicativeExpression(token);
        List<Kind> validSymbols = Arrays.asList(Kind.PLUS, Kind.MINUS);
        IPLPToken nextToken = lexer.peekNextToken();
        if(validSymbols.contains(nextToken.getKind())){
            IPLPToken opToken = lexer.nextToken();
            token = lexer.nextToken();
            IExpression rightExpression = parseMultiplicativeExpression(token);
            return new BinaryExpression__(line, posInLine, text, leftExpression, rightExpression, opToken.getKind());
        }
        return leftExpression;
    }

    private IExpression parseMultiplicativeExpression(IPLPToken token) throws LexicalException, SyntaxException {
        int line = token.getLine();
        int posInLine = token.getCharPositionInLine();
        String text = token.getText();
        IExpression leftExpression = parseUnaryExpression(token);
        List<Kind> validSymbols = Arrays.asList(Kind.TIMES, Kind.DIV);
        IPLPToken nextToken = lexer.peekNextToken();
        if(validSymbols.contains(nextToken.getKind())){
            IPLPToken opToken = lexer.nextToken();
            token = lexer.nextToken();
            IExpression rightExpression = parseUnaryExpression(token);
            return new BinaryExpression__(line, posInLine, text, leftExpression, rightExpression, opToken.getKind());
        }
        return leftExpression;
    }

    private IExpression parseUnaryExpression(IPLPToken token) throws LexicalException, SyntaxException {
        int line = token.getLine();
        int posInLine = token.getCharPositionInLine();
        String text = token.getText();
        List<Kind> validSymbols = Arrays.asList(Kind.BANG, Kind.MINUS);
        IPLPToken nextToken = lexer.peekNextToken();
        if(validSymbols.contains(nextToken.getKind())){
            IPLPToken opToken = lexer.nextToken();
            token = lexer.nextToken();
            IExpression primaryExpression = parsePrimaryExpression(token);
            return new UnaryExpression__(line, posInLine, text, primaryExpression, opToken.getKind());
        }
        return parsePrimaryExpression(token);
    }

    private IExpression parsePrimaryExpression(IPLPToken  token) throws LexicalException, SyntaxException {
        int line = token.getLine();
        int posInLine = token.getCharPositionInLine();
        String text = token.getText();

        switch (token.getKind()){
            case KW_NIL -> {
                return new NilConstantExpression__(line, posInLine, text);
            }
            case KW_TRUE -> {
                return new BooleanLiteralExpression__(line, posInLine, text, true);
            }
            case KW_FALSE -> {
                return new BooleanLiteralExpression__(line, posInLine, text, false);
            }
            case STRING_LITERAL -> {
                return new StringLiteralExpression__(line, posInLine, text, token.getStringValue());
            }
            case INT_LITERAL -> {
                return new IntLiteralExpression__(line, posInLine, text, token.getIntValue());
            }
            case LPAREN -> {
                token = lexer.nextToken();
                IExpression expression= parseExpression(token);
                token = lexer.nextToken();
                if (token.getKind() != Kind.RPAREN){
                    throw new SyntaxException("Expecting Right paren ')'", token.getLine(),  token.getCharPositionInLine());
                }
                return expression;
            }
            case IDENTIFIER -> {
                IIdentifier idName = new Identifier__(line, posInLine, text, text);
                List<IExpression> args = new ArrayList<>();
                IPLPToken nextToken = lexer.peekNextToken();
                if(nextToken.getKind() == Kind.LPAREN){
                    lexer.nextToken();
                    token = lexer.nextToken();
                    if(token.getKind() != Kind.RPAREN) {
                        args.add(parseExpression(token));
                        while (token.getKind() == Kind.COMMA) {
                            token = lexer.nextToken();
                            if(token.getKind() != Kind.RPAREN){
                                args.add(parseExpression(token));
                            }
                        }
                    }
                    if (token.getKind() != Kind.RPAREN) {
                        throw new SyntaxException("Expecting close paren ')'", token.getLine(), token.getCharPositionInLine());
                    }
                    return new FunctionCallExpression__(line, posInLine, text, idName, args);
                    //token = lexer.nextToken();
                }
                else if(nextToken.getKind() == Kind.LSQUARE){
                    lexer.nextToken();
                    token = lexer.nextToken();
                    IExpression idx = parseExpression(token);
                    if(token.getKind() != Kind.RSQUARE){
                        throw new SyntaxException("Expecting close sq paren ']'", token.getLine(), token.getCharPositionInLine());
                    }
                    //token = lexer.nextToken();
                    return new ListSelectorExpression__(line, posInLine, text, idName, idx);
                }
                else{
                    return new IdentExpression__(line, posInLine, text, idName);
                }
            }

            default ->
                throw new SyntaxException("""
                Expecting NIL | TRUE | FALSE |  IntLiteral | StringLiteral   |  ( Expression ) |
                    Identifier  ( (Expression ( , Expression)* )? )  |
                    Identifier |  Identifier [ Expression ]   \s
                """,
                    token.getLine(),
                    token.getCharPositionInLine());
        }

    }

    private IType parseFunctionReturnType(IPLPToken token) throws SyntaxException, LexicalException {
        if(token.getKind() == Kind.KW_DO){
            return null;
        }

        if(token.getKind() != Kind.COLON){
            throw new SyntaxException("Expecting keyword 'DO 'or colon (for return type def) ':'", token.getLine(),  token.getCharPositionInLine());
        }

        token = lexer.nextToken();
        return  parseType(token);
    }

    private IType parseType(IPLPToken token) throws LexicalException, SyntaxException {
        IType type;
        int line = token.getLine();
        int posInLine = token.getCharPositionInLine();
        String text = token.getText();

        if(token.getKind() == Kind.RSQUARE){
            return null;
        }
        Map<Kind, IType.TypeKind> validPrimitivesMap = Stream.of(new Object[][] {
                { Kind.KW_INT, IType.TypeKind.INT},
                { Kind.KW_STRING, IType.TypeKind.STRING },
                { Kind.KW_BOOLEAN, IType.TypeKind.BOOLEAN}
        }).collect(Collectors.toMap(data -> (Kind) data[0], data -> (IType.TypeKind) data[1]));

        if(validPrimitivesMap.containsKey(token.getKind())){
            type = new PrimitiveType__(line, posInLine, text, validPrimitivesMap.get(token.getKind()));
            return type;
        }

        if(token.getKind() != Kind.KW_LIST){
            throw new SyntaxException("Expecting keyword 'List 'or primitives 'INT, BOOLEAN, STRING'", token.getLine(),  token.getCharPositionInLine());
        }


        token = lexer.nextToken();
        if (token.getKind() != Kind.LSQUARE){
            throw new SyntaxException("Expecting keyword Left square paren '['", token.getLine(),  token.getCharPositionInLine());
        }

        token = lexer.nextToken();
        IType listType =parseType(token);
        if(listType != null){
            token = lexer.nextToken();
        }

        if (token.getKind() != Kind.RSQUARE){
            throw new SyntaxException("Expecting keyword Right square paren ']'", token.getLine(),  token.getCharPositionInLine());
        }

        return new ListType__(line, posInLine, text, listType);
    }

    private INameDef parseNameDef(IPLPToken token) throws SyntaxException, LexicalException {
        INameDef nameDef;
        IIdentifier id;
        IType type =  null;
        int line, posInLine;
        String text;
        if(token.getKind() != Kind.IDENTIFIER){
            throw new SyntaxException("Expecting token of type 'Identifier' ", token.getLine(),  token.getCharPositionInLine());
        }
        line = token.getLine();
        posInLine = token.getCharPositionInLine();
        text = token.getText();
        id = new Identifier__(line, posInLine, text, text);
        IPLPToken nextToken = lexer.peekNextToken();
        if(nextToken.getKind() == Kind.COLON){
            lexer.nextToken();
            type = parseType(lexer.nextToken());
            nameDef = new NameDef__(line, posInLine, text, id, type);
            return nameDef;
        }
        nameDef = new NameDef__(line, posInLine, text, id, type);
        return nameDef;
    }

    private static class PrevTokenWrapper<N extends IASTNode>{
        IPLPToken prevToken;
        N astNode;
        public PrevTokenWrapper(IPLPToken token, N node) {
            prevToken = token;
            astNode = node;
        }
    }

}
