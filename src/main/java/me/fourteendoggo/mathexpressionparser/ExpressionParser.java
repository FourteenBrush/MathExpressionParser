package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.function.FunctionCallSite;
import me.fourteendoggo.mathexpressionparser.function.FunctionContext;

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
        Assert.notNull(input, "input was null");
        Assert.notBlank(input);

        return new Expression(input.toCharArray()).parse();
    }

    /**
     * @see #insertFunction(String, int, int, ToDoubleFunction)
     */
    public static void insertFunction(String functionName, DoubleUnaryOperator function) {
        Tokenizer.FUNCTION_CONTAINER.insertFunction(functionName, function);
    }

    /**
     * @see #insertFunction(String, int, int, ToDoubleFunction)
     */
    public static void insertFunction(String functionName, DoubleBinaryOperator function) {
        Tokenizer.FUNCTION_CONTAINER.insertFunction(functionName, function);
    }

    /**
     * Inserts a function with a variable amount of parameters
     * @param functionName the name of the function, only lowercase characters
     * @param minArgs the minimum amount of arguments the function can have, can be the same as maxArgs
     * @param maxArgs the maximum amount of arguments the function can have, can be the same as minArgs
     * @param function the function to call
     */
    public static void insertFunction(String functionName, int minArgs, int maxArgs, ToDoubleFunction<FunctionContext> function) {
        Tokenizer.FUNCTION_CONTAINER.insertFunction(new FunctionCallSite(functionName, minArgs, maxArgs, function));
    }
}
