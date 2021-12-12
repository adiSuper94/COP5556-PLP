package edu.ufl.cise.plpfa21.assignment5;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomTests_Aditya_Subramanian {
    CodeGenTests cgt = new CodeGenTests();
    @DisplayName("let2_int")
    @Test
    public void let2_int(TestInfo testInfo) throws Exception {
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
}
