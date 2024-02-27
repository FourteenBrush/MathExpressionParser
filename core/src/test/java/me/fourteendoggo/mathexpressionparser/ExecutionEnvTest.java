package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.environment.ExecutionEnv;
import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

// TODO
public class ExecutionEnvTest {
    private ExecutionEnv env;

    @BeforeEach
    void setUp() {
        env = new ExecutionEnv();
    }

    @Test
    void ensureNonExistentFunctionsDontExist() {
        String[] nonExistentFunctions = new String[] {
                "some_weird_function",
                "some_other_function",
                "some_other_other_function",
        };

        for (String function : nonExistentFunctions) {
            assertThatCode(() -> ExpressionParser.parse(function + "()", env)).isInstanceOf(SyntaxException.class);
        }
    }
}
