package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.exceptions.SymbolNotFoundException;
import me.fourteendoggo.mathexpressionparser.function.FunctionCallSite;
import me.fourteendoggo.mathexpressionparser.symbol.ExecutionEnv;
import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.symbol.Symbol;
import me.fourteendoggo.mathexpressionparser.symbol.Variable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

public class ExecutionEnvTest {
    private ExecutionEnv env;

    @BeforeEach
    void setUp() {
        env = ExecutionEnv.empty();
    }

    // TODO: revision
    static Stream<Arguments> provideEnvironments() {
        return Stream.of(
                ExecutionEnv.empty(),
                ExecutionEnv.defaulted()
        ).map(Arguments::of);
    }

    @Test
    void ensureNonExistentFunctionsDontExist() {
        String[] nonExistentFunctions = {
                "some_weird_function",
                "some_other_function",
                "some_other_other_function",
        };

        for (String function : nonExistentFunctions) {
            assertThatThrownBy(() -> ExpressionParser.parse(function + "()", env))
                    .isInstanceOf(SymbolNotFoundException.class)
                    .hasMessageContaining("not found")
                    .satisfies(e -> {
                        String symbol = ((SymbolNotFoundException) e).getSymbol();
                        assertThat(symbol).isEqualTo(function);
                    });
        }
    }

    @Test
    void testThrowingFunctionConstructor() {
        assertThatThrownBy(() -> new FunctionCallSite("smth", 3, 2, ctx -> 1))
                .isInstanceOf(SyntaxException.class);

        assertThatThrownBy(() -> new FunctionCallSite("smth", -2, 1, ctx -> 1))
                .isInstanceOf(SyntaxException.class);

        assertThatThrownBy(() -> new FunctionCallSite("smth", -5, -1, ctx -> 1))
                .isInstanceOf(SyntaxException.class);

        assertThatThrownBy(() -> new FunctionCallSite("smth", 0, -2, ctx -> 1))
                .isInstanceOf(SyntaxException.class);
    }

    @ParameterizedTest
    @MethodSource("provideEnvironments")
    void testInvalidIdentifierNames(ExecutionEnv env) {
        String[] names = {
                "", "#", "'", "^", "$",
                ")-", "(a", "ë"
        };

        for (String name : names) {
            assertThatThrownBy(() -> env.insertVariable(name, 2))
                    .isInstanceOf(SyntaxException.class)
                    .hasMessageContaining("invalid identifier name");
        }
    }

    @ParameterizedTest
    @MethodSource("provideEnvironments")
    void ensureThrowingOnAlreadyExistentSymbolKeepsStateConsistent(ExecutionEnv env) {
        env.insertFunction("a", () -> 1);

        assertThatThrownBy(() -> env.insertFunction("a", () -> 3))
                .isInstanceOf(SyntaxException.class);

        assertThat(ExpressionParser.parse("a()", env)).isEqualTo(1);
    }

    @ParameterizedTest
    @MethodSource("provideEnvironments")
    void insertIfAbsentOnAbsentVariable(ExecutionEnv env) {
        assertThat(env.insertVariableIfAbsent("a", 2)).isNull();
        assertThat(env.insertVariableIfAbsent("a", 2)).isNull();
    }

    @ParameterizedTest
    @MethodSource("provideEnvironments")
    void insertIfAbsentOnPresentVariable(ExecutionEnv env) {
        env.insertVariable("a", 3);

        assertThatCode(() -> {
            Symbol sym = env.insertVariableIfAbsent("a", 3);
            assertThat(sym).isInstanceOf(Variable.class).matches(s -> s.getName().equals("a"));
        }).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("provideEnvironments")
    void insertIfAbsentOnAbsentSymbol(ExecutionEnv env) {
        assertThatCode(() -> env.insertFunction("a", () -> 1)).doesNotThrowAnyException();
        assertThatCode(() -> {
            Symbol a = env.lookupSymbol("a()".toCharArray(), 0);
            assertThat(a.getName()).isEqualTo("a");
        }).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("provideEnvironments")
    void insertIfAbsentOnPresentFunction_supplier(ExecutionEnv env) {
        env.insertFunction("a", () -> 1);

        assertThatCode(() -> {
            Symbol sym = env.insertFunctionIfAbsent("a", () -> 2);
            assertThat(sym.getName()).isEqualTo("a");

            sym = env.insertFunctionIfAbsent("a", () -> 3);
            assertThat(sym.getName()).isEqualTo("a");
        }).doesNotThrowAnyException();

        assertThat(ExpressionParser.parse("a()", env)).isEqualTo(1);
    }

    @ParameterizedTest
    @MethodSource("provideEnvironments")
    void insertIfAbsentOnPresentFunction_unaryOperator(ExecutionEnv env) {
        env.insertFunction("a", a -> 1);

        assertThatCode(() -> {
            Symbol sym = env.insertFunctionIfAbsent("a", a -> 2);
            assertThat(sym.getName()).isEqualTo("a");

            sym = env.insertFunctionIfAbsent("a", a -> 3);
            assertThat(sym.getName()).isEqualTo("a");
        }).doesNotThrowAnyException();

        assertThat(ExpressionParser.parse("a(1)", env)).isEqualTo(1);
    }

    @ParameterizedTest
    @MethodSource("provideEnvironments")
    void insertIfAbsentOnPresentFunction_binaryOperator(ExecutionEnv env) {
        env.insertFunction("a", (a, b) -> 1);

        assertThatCode(() -> {
            Symbol sym = env.insertFunctionIfAbsent("a", (a, b) -> 2);
            assertThat(sym.getName()).isEqualTo("a");

            sym = env.insertFunctionIfAbsent("a", (a, b) -> 3);
            assertThat(sym.getName()).isEqualTo("a");
        }).doesNotThrowAnyException();

        assertThat(ExpressionParser.parse("a(1, 2)", env)).isEqualTo(1);
    }

    @ParameterizedTest
    @MethodSource("provideEnvironments")
    void insertIfAbsentOnPresentFunction_toDoubleFunction1(ExecutionEnv env) {
        env.insertFunction("a", 0, ctx -> 1);

        assertThatCode(() -> {
            Symbol sym = env.insertFunctionIfAbsent("a", 0, ctx -> 2);
            assertThat(sym.getName()).isEqualTo("a");

            sym = env.insertFunctionIfAbsent("a", 0, ctx -> 3);
            assertThat(sym.getName()).isEqualTo("a");
        }).doesNotThrowAnyException();

        assertThat(ExpressionParser.parse("a()", env)).isEqualTo(1);
    }

    @ParameterizedTest
    @MethodSource("provideEnvironments")
    void insertIfAbsentOnPresentFunction_toDoubleFunction2(ExecutionEnv env) {
        env.insertFunction("a", 0, 1, ctx -> 1);

        assertThatCode(() -> {
            Symbol sym = env.insertFunctionIfAbsent("a", 0, 1, ctx -> 2);
            assertThat(sym.getName()).isEqualTo("a");

            sym = env.insertFunctionIfAbsent("a", 0, 1, ctx -> 3);
            assertThat(sym.getName()).isEqualTo("a");
        }).doesNotThrowAnyException();

        assertThat(ExpressionParser.parse("a()", env)).isEqualTo(1);
    }

    @ParameterizedTest
    @MethodSource("provideEnvironments")
    void insertIfAbsentOnPresentFunction_toDoubleFunction_differentParamCount(ExecutionEnv env) {
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
