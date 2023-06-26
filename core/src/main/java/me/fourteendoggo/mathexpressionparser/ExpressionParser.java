package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.function.FunctionCallSite;
import me.fourteendoggo.mathexpressionparser.function.FunctionContext;
import me.fourteendoggo.mathexpressionparser.utils.Assert;

import java.util.Objects;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleFunction;

public class ExpressionParser {

    private ExpressionParser() {}

    /**
     * Parses the given expression and returns the result
     *
     * @param input the expression to parse
     * @return the result of the expression as a double
     * @throws SyntaxException if the given expression is invalid or empty
     */
    public static double parse(String input) {
        Objects.requireNonNull(input, "input was null");
        Assert.isFalse(input.isEmpty(), "input was empty");

        Tokenizer tokenizer = new Tokenizer(input.toCharArray());
        return tokenizer.readTokens().solve();
    }

    /**
     * @see #insertFunction(String, int, int, ToDoubleFunction)
     */
    public static void insertFunction(String functionName, DoubleUnaryOperator function) {
        Tokenizer.getFunctionContainer().insertFunction(functionName, function);
    }

    /**
     * @see #insertFunction(String, int, int, ToDoubleFunction)
     */
    public static void insertFunction(String functionName, DoubleBinaryOperator function) {
        Tokenizer.getFunctionContainer().insertFunction(functionName, function);
    }

    /**
     * Inserts a function with a variable amount of parameters
     * @param functionName the name of the function, only lowercase characters
     * @param minArgs the minimum amount of arguments the function can have, can be the same as maxArgs
     * @param maxArgs the maximum amount of arguments the function can have, can be the same as minArgs
     * @param function the function to call
     */
    public static void insertFunction(String functionName, int minArgs, int maxArgs, ToDoubleFunction<FunctionContext> function) {
        Tokenizer.getFunctionContainer().insertFunction(new FunctionCallSite(functionName, minArgs, maxArgs, function));
    }

    /**
     * Inserts a complex function
     * @param function the function to insert
     */
    public static void insertFunction(FunctionCallSite function) {
        Tokenizer.getFunctionContainer().insertFunction(function);
    }
}
