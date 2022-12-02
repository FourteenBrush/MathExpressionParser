package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;

public class ExpressionParser {

    /**
     * Parses the given expression and returns the result
     *
     * @param input the expression to parse
     * @return the result of the expression as a double
     * @throws SyntaxException if the given expression is invalid or empty
     */
    public static double parse(String input) {
        Validate.notBlank(input);

        return new Expression(input.toCharArray()).parse();
    }
}
