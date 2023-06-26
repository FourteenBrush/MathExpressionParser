package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.environment.ExecutionEnv;
import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.function.FunctionCallSite;
import me.fourteendoggo.mathexpressionparser.function.FunctionContext;
import me.fourteendoggo.mathexpressionparser.symbol.Variable;
import me.fourteendoggo.mathexpressionparser.token.Tokenizer;
import me.fourteendoggo.mathexpressionparser.utils.Assert;

import java.util.Objects;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleFunction;

public class ExpressionParser {
    private static ExecutionEnv DEFAULT_ENV;

    private ExpressionParser() {}

    public static double parse(String input) {
        return parse(input, getDefaultEnv());
    }

    /**
     * Parses the given expression and returns the result
     *
     * @param input the expression to parse
     * @param env the symbol lookup to obtain symbols from
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
        insertFunction(new FunctionCallSite(functionName, 0, ctx -> fn.getAsDouble()));
    }

    /**
     * @see #insertFunction(String, int, int, ToDoubleFunction)
     */
    public static void insertFunction(String functionName, DoubleUnaryOperator fn) {
        getDefaultEnv().insertFunction(functionName, fn);
    }

    /**
     * @see #insertFunction(String, int, int, ToDoubleFunction)
     */
    public static void insertFunction(String functionName, DoubleBinaryOperator fn) {
        getDefaultEnv().insertFunction(functionName, fn);
    }

    /**
     * Inserts a function with a variable amount of parameters into the default symbol lookup.
     * @param functionName the name of the function, only lowercase characters
     * @param minArgs the minimum amount of arguments the function can have, can be the same as maxArgs
     * @param maxArgs the maximum amount of arguments the function can have, can be the same as minArgs
     * @param fn the function to call
     */
    public static void insertFunction(String functionName, int minArgs, int maxArgs, ToDoubleFunction<FunctionContext> fn) {
        getDefaultEnv().insertFunction(functionName, minArgs, maxArgs, fn);
    }

    /**
     * Inserts a complex function
     * @param function the function to insert
     */
    public static void insertFunction(FunctionCallSite function) {
        getDefaultEnv().insertSymbol(function);
    }

    /**
     * Inserts a variable into the default symbol lookup.
     * @param variable the variable to insert.
     */
    public static void insertVariable(Variable variable) {
        getDefaultEnv().insertSymbol(variable);
    }

    public static ExecutionEnv getDefaultEnv() {
        if (DEFAULT_ENV == null) {
            DEFAULT_ENV = new ExecutionEnv();
        }
        return DEFAULT_ENV;
    }
}
