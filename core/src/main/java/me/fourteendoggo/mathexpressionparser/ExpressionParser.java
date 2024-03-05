package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.symbol.ExecutionEnv;
import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.function.FunctionCallSite;
import me.fourteendoggo.mathexpressionparser.function.FunctionContext;
import me.fourteendoggo.mathexpressionparser.symbol.Symbol;
import me.fourteendoggo.mathexpressionparser.symbol.Variable;
import me.fourteendoggo.mathexpressionparser.token.Tokenizer;

import java.util.Objects;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleFunction;

import static me.fourteendoggo.mathexpressionparser.ExpressionParser.DefaultEnvHolder.DEFAULT_ENV;

/**
 * Frontend of the expression parser.
 */
@SuppressWarnings("unused")
public class ExpressionParser {

    private ExpressionParser() {}

    /**
     * @see ExpressionParser#parse(String, ExecutionEnv)
     */
    public static double parse(String input) {
        return parse(input, DEFAULT_ENV);
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

        Tokenizer tokenizer = new Tokenizer(input.toCharArray(), env);
        return tokenizer.readTokens().solve();
    }

    /**
     * @see #insertFunction(String, int, int, ToDoubleFunction)
     */
    public static void insertFunction(String functionName, DoubleSupplier fn) {
        DEFAULT_ENV.insertFunction(functionName, fn);
    }

    /**
     * @see ExecutionEnv#insertFunction(String, DoubleUnaryOperator)
     */
    public static void insertFunction(String functionName, DoubleUnaryOperator fn) {
        DEFAULT_ENV.insertFunction(functionName, fn);
    }

    /**
     * @see ExecutionEnv#insertFunction(String, DoubleBinaryOperator)
     */
    public static void insertFunction(String functionName, DoubleBinaryOperator fn) {
        DEFAULT_ENV.insertFunction(functionName, fn);
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
        DEFAULT_ENV.insertFunction(functionName, minArgs, maxArgs, fn);
    }

    /**
     * Inserts a symbol into the default execution environment.
     * @param symbol the symbol to insert.
     * @see FunctionCallSite
     * @see Variable
     */
    public static void insertSymbol(Symbol symbol) {
        DEFAULT_ENV.insertSymbol(symbol);
    }

    static class DefaultEnvHolder {
        static final ExecutionEnv DEFAULT_ENV = ExecutionEnv.createDefault();
    }
}
