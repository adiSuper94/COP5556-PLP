package edu.ufl.cise.plpfa21.assignment5;

import edu.ufl.cise.plpfa21.assignment1.PLPTokenKinds;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomTests_Aditya_Subramanian {
    CodeGenTests cgt = new CodeGenTests();
    @DisplayName("aditya_subramanian_test0")
    @Test
    public void aditya_subramanian_test0(TestInfo testInfo) throws Exception {
        String input = """
				FUN a(): INT
				DO
				   LET y:INT DO LET x = 1 DO y = x; RETURN y+y; END END
				END
				""";
        byte[] bytecode = cgt.compile(input, CodeGenTests.className, CodeGenTests.packageName);
        //show(CodeGenUtils.bytecodeToString(bytecode));
        Object[] params = {};
        Integer result = (Integer) cgt.loadClassAndRunMethod(bytecode, CodeGenTests.className, "a", params);
        assertEquals(2, result);
    }

    @DisplayName("aditya_subramanian_test1")
    @Test
    public void aditya_subramanian_test1(TestInfo testInfo) throws Exception {
        String input = """
				FUN factorial(x:INT):INT
				DO
				   LET F = 1 DO END
				   WHILE x > 0
				    DO
				        F = F * x;
				        x = x - 1;
				    END
				   RETURN F;
				END
				""";
        byte[] bytecode = cgt.compile(input, CodeGenTests.className, CodeGenTests.packageName);
        List<Object[]> paramList = Stream.of(new Object[][] {
                { 1 }, { 2 }, { 3 }, { 4 }, { 5 }, { 6 }, { 7 }, { 8 }, { 9 }, { 10 }
        }).collect(Collectors.toList());
        List<Integer> results = Arrays.asList(1, 2, 6, 24, 120, 720, 5040, 40320, 362880, 3628800);
        for(int i = 0; i < 10; i++){
            Object[] params = paramList.get(i);
            Integer expected = results.get(i);
            Integer result = (Integer) cgt.loadClassAndRunMethod(bytecode, CodeGenTests.className, "factorial", params);
            assertEquals(expected, result);
        }

    }

    @DisplayName("aditya_subramanian_test2")
    @Test
    public void aditya_subramanian_test2(TestInfo testInfo) throws Exception {
        String input = """
                VAR f = 0;
                VAR s = 1;
				FUN fib(x:INT):INT
				DO
				    LET  fibN = 0 DO f = 0; END
				    IF x == 1
				    DO
		                RETURN f;
				    END
				   
				     IF x == 2
				     DO
				        RETURN s;
				     END
				     WHILE x > 2
				     DO
				        fibN = f + s;
				        f = s;
				        s = fibN;
				        x = x - 1;
				    END
				    RETURN fibN;
				END
                """;
        byte[] bytecode = cgt.compile(input, CodeGenTests.className, CodeGenTests.packageName);


        List<Object[]> paramList = Stream.of(new Object[][] {
                { 1 }, { 2 }, { 3 }, { 4 }, { 5 }, { 6 }, { 7 }, { 8 }, { 9 }, { 10 }, { 11 }, { 12 }
        }).collect(Collectors.toList());
        List<Integer> results = Arrays.asList(0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89);
        for(int i = 0; i < 12; i++){
            Object[] params = paramList.get(i);
            Integer expected = results.get(i);
            Integer result = (Integer) cgt.loadClassAndRunMethod(bytecode, CodeGenTests.className, "fib", params);
            assertEquals(expected, result);
        }

    }
}
