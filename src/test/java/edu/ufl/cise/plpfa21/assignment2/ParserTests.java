package edu.ufl.cise.plpfa21.assignment2;



import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.ufl.cise.plpfa21.assignment1.CompilerComponentFactory;


class ParserTests {

    static boolean VERBOSE = true;

    void noErrorParse(String input)  {
        IPLPParser parser = CompilerComponentFactory.getParser(input);
        try {
            parser.parse();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    private void syntaxErrorParse(String input, int line, int column) {
        IPLPParser parser = CompilerComponentFactory.getParser(input);
        assertThrows(SyntaxException.class, () -> {
            try {
                parser.parse();
            }
            catch(SyntaxException e) {
                if (VERBOSE) System.out.println(input + '\n' + e.getMessage());
                Assertions.assertEquals(line, e.line);
                Assertions.assertEquals(column, e.column);
                throw e;
            }
        });

    }

    //This input has a syntax error at line 2, position 19.
    @Test
    public void testVarInFunctionFailure()  {
        String input = """
		FUN func() DO
		VAR a = 1;
		END
		""";
        syntaxErrorParse(input,2,0);
    }

    @Test
    public void testSwitch(){
        String input = """
                FUN dooo()
                DO
                    LET a = 1;
                    LET b = 2;
                    SWITCH a
                        CASE 1:
                            a = a + 1;
                            b = b + 1;
                    
                        CASE 2:
                            a = a + b;
                            b = a;
                        DEFAULT
                            LET X;
                            LET X = a;
                            a = b;
                            b = x;
                    END
                END
                """;
        noErrorParse(input);
    }



}
