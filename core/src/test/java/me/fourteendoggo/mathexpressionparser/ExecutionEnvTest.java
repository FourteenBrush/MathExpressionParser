package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.symbol.ExecutionEnv;
import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.symbol.Symbol;
import me.fourteendoggo.mathexpressionparser.symbol.Variable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.*;

// TODO
public class ExecutionEnvTest {
    private ExecutionEnv env;

    @BeforeEach
    void setUp() {
        env = ExecutionEnv.empty();
    }

    @Test
    void ensureNonExistentFunctionsDontExist() {
        String[] nonExistentFunctions = new String[]{
                "some_weird_function",
                "some_other_function",
                "some_other_other_function",
        };

        for (String function : nonExistentFunctions) {
            assertThatThrownBy(() -> ExpressionParser.parse(function + "()", env)).isInstanceOf(SyntaxException.class);
        }
    }

    @Test
    void ensureThrowingOnAlreadyExistentSymbolKeepsStateConsistent() {
        env.insertFunction("a", () -> 1);

        assertThatThrownBy(() -> env.insertFunction("a", () -> 3))
                .isInstanceOf(SyntaxException.class);

        assertThat(ExpressionParser.parse("a()", env)).isEqualTo(1);
    }

    @Test
    void insertIfAbsentOnAbsentVariable() {
        assertThat(env.insertVariableIfAbsent("a", 2)).isNull();
        assertThat(env.insertVariableIfAbsent("a", 2)).isNull();
    }

    @Test
    void insertIfAbsentOnPresentVariable() {
        env.insertVariable("a", 3);

        assertThatCode(() -> {
            Symbol sym = env.insertVariableIfAbsent("a", 3);
            assertThat(sym).isInstanceOf(Variable.class).matches(s -> s.getName().equals("a"));
        }).doesNotThrowAnyException();
    }

    @Test
    void insertIfAbsentOnAbsentSymbol() {
        assertThatCode(() -> env.insertFunction("a", () -> 1)).doesNotThrowAnyException();
        assertThatCode(() -> {
            Symbol a = env.lookupSymbol("a()".toCharArray(), 0);
            assertThat(a.getName()).isEqualTo("a");
        }).doesNotThrowAnyException();
    }

    @Test
    void insertIfAbsentOnPresentFunction_supplier() {
        env.insertFunction("a", () -> 1);

        assertThatCode(() -> {
            Symbol sym = env.insertFunctionIfAbsent("a", () -> 2);
            assertThat(sym.getName()).isEqualTo("a");

            sym = env.insertFunctionIfAbsent("a", () -> 3);
            assertThat(sym.getName()).isEqualTo("a");
        }).doesNotThrowAnyException();

        assertThat(ExpressionParser.parse("a()", env)).isEqualTo(1);
    }

    @Test
    void insertIfAbsentOnPresentFunction_unaryOperator() {
        env.insertFunction("a", a -> 1);

        assertThatCode(() -> {
            Symbol sym = env.insertFunctionIfAbsent("a", a -> 2);
            assertThat(sym.getName()).isEqualTo("a");

            sym = env.insertFunctionIfAbsent("a", a -> 3);
            assertThat(sym.getName()).isEqualTo("a");
        }).doesNotThrowAnyException();

        assertThat(ExpressionParser.parse("a(1)", env)).isEqualTo(1);
    }

    @Test
    void insertIfAbsentOnPresentFunction_binaryOperator() {
        env.insertFunction("a", (a, b) -> 1);

        assertThatCode(() -> {
            Symbol sym = env.insertFunctionIfAbsent("a", (a, b) -> 2);
            assertThat(sym.getName()).isEqualTo("a");

            sym = env.insertFunctionIfAbsent("a", (a, b) -> 3);
            assertThat(sym.getName()).isEqualTo("a");
        }).doesNotThrowAnyException();

        assertThat(ExpressionParser.parse("a(1, 2)", env)).isEqualTo(1);
    }

    @Test
    void insertIfAbsentOnPresentFunction_toDoubleFunction1() {
        env.insertFunction("a", 0, ctx -> 1);

        assertThatCode(() -> {
            Symbol sym = env.insertFunctionIfAbsent("a", 0, ctx -> 2);
            assertThat(sym.getName()).isEqualTo("a");

            sym = env.insertFunctionIfAbsent("a", 0, ctx -> 3);
            assertThat(sym.getName()).isEqualTo("a");
        }).doesNotThrowAnyException();

        assertThat(ExpressionParser.parse("a()", env)).isEqualTo(1);
    }

    @Test
    void insertIfAbsentOnPresentFunction_toDoubleFunction2() {
        env.insertFunction("a", 0, 1, ctx -> 1);

        assertThatCode(() -> {
            Symbol sym = env.insertFunctionIfAbsent("a", 0, 1, ctx -> 2);
            assertThat(sym.getName()).isEqualTo("a");

            sym = env.insertFunctionIfAbsent("a", 0, 1, ctx -> 3);
            assertThat(sym.getName()).isEqualTo("a");
        }).doesNotThrowAnyException();

        assertThat(ExpressionParser.parse("a()", env)).isEqualTo(1);
    }

    @Test
    void insertIfAbsentOnPresentFunction_toDoubleFunction_differentParamCount() {
        env.insertFunction("a", 0, ctx -> 1);

        assertThatCode(() -> {
            Symbol sym = env.insertFunctionIfAbsent("a", 1, 4, ctx -> 2);
            assertThat(sym.getName()).isEqualTo("a");

            sym = env.insertFunctionIfAbsent("a", 2, 8, ctx -> 3);
            assertThat(sym.getName()).isEqualTo("a");
        }).doesNotThrowAnyException();

        assertThat(ExpressionParser.parse("a()", env)).isEqualTo(1);
    }
}
