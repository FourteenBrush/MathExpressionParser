package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.environment.ExecutionEnv;
import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.function.FunctionCallSite;
import me.fourteendoggo.mathexpressionparser.function.FunctionContext;
import me.fourteendoggo.mathexpressionparser.symbol.Symbol;
import me.fourteendoggo.mathexpressionparser.symbol.Variable;
import me.fourteendoggo.mathexpressionparser.token.Tokenizer;
import me.fourteendoggo.mathexpressionparser.utils.Assert;

import java.util.Objects;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleFunction;

@SuppressWarnings("unused")
public class ExpressionParser {
    private static ExecutionEnv DEFAULT_ENV;

    private ExpressionParser() {}

    /**
     * @see ExpressionParser#parse(String, ExecutionEnv)
     */
    public static double parse(String input) {
        return parse(input, getDefaultEnv());
    }

    /**
     * Parses the given expression and returns the result
     *
     * @param input the expression to parse
     * @param env the execution environment to obtain symbols from
     * @return the result of the expression
     * @throws NullPointerException if the expression or env is null
     * @throws SyntaxException if the given expression is invalid or empty
     */
    public static double parse(String input, ExecutionEnv env) {
        Objects.requireNonNull(input, "input was null");
        Objects.requireNonNull(env, "environment was null");
        Assert.isFalse(input.isEmpty(), "cannot solve an empty expression");

        Tokenizer tokenizer = new Tokenizer(input.toCharArray(), env);
        return tokenizer.readTokens().solve();
    }

    /**
     * @see #insertFunction(String, int, int, ToDoubleFunction)
     */
    public static void insertFunction(String functionName, DoubleSupplier fn) {
        getDefaultEnv().insertFunction(functionName, fn);
    }

    /**
     * @see ExecutionEnv#insertFunction(String, DoubleUnaryOperator)
     */
    public static void insertFunction(String functionName, DoubleUnaryOperator fn) {
        getDefaultEnv().insertFunction(functionName, fn);
    }

    /**
     * @see ExecutionEnv#insertFunction(String, DoubleBinaryOperator)
     */
    public static void insertFunction(String functionName, DoubleBinaryOperator fn) {
        getDefaultEnv().insertFunction(functionName, fn);
    }

    /**
     * Inserts a function into the default execution environment.
     * @see ExecutionEnv#insertFunction(String, int, ToDoubleFunction)
     */
    public static void insertFunction(String functionName, int numArgs, ToDoubleFunction<FunctionContext> fn) {
        insertFunction(functionName, numArgs, numArgs, fn);
    }

    /**
     * Inserts a function into the default execution environment.
     * @see ExecutionEnv#insertFunction(String, int, int, ToDoubleFunction)
     */
    public static void insertFunction(String functionName, int minArgs, int maxArgs, ToDoubleFunction<FunctionContext> fn) {
        getDefaultEnv().insertFunction(functionName, minArgs, maxArgs, fn);
    }

    /**
     * Inserts a symbol into the default execution environment.
     * @param symbol the symbol to insert, can be either a variable or a function
     * @see FunctionCallSite
     * @see Variable
     */
    public static void insertSymbol(Symbol symbol) {
        getDefaultEnv().insertSymbol(symbol);
    }

    private static ExecutionEnv getDefaultEnv() {
        if (DEFAULT_ENV == null) {
            DEFAULT_ENV = new ExecutionEnv();
        }
        return DEFAULT_ENV;
    }
}
