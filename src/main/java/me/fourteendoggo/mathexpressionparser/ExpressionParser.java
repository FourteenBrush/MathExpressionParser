package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;

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

    public static void insertFunction(String function, ToDoubleFunction<double[]> solver) {
        Tokenizer.FUNCTION_CONTAINER.insert(function, solver);
    }
}
