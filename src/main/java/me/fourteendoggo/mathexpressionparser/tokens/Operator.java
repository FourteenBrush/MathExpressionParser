package me.fourteendoggo.mathexpressionparser.tokens;

import me.fourteendoggo.mathexpressionparser.Token;
import me.fourteendoggo.mathexpressionparser.TokenType;

import java.util.function.DoubleBinaryOperator;

public enum Operator implements Token {
    POWER('^', 3, Math::pow),
    MULTIPLICATION('*', 2, (a, b) -> a * b),
    DIVISION('/', 2, (a, b) -> a / b),
    MODULO('%', 2, (a, b) -> a % b),
    ADDITION('+', 1, Double::sum),
    SUBTRACTION('-', 1, (a, b) -> a - b);

    public static final int HIGHEST_PRIORITY = 3;

    private final char symbol;
    private final int priority;
    private final DoubleBinaryOperator solver;

    Operator(char symbol, int priority, DoubleBinaryOperator solver) {
        this.symbol = symbol;
        this.priority = priority;
        this.solver = solver;
    }

    @Override
    public TokenType getType() {
        return TokenType.OPERATOR;
    }

    public char getSymbol() {
        return symbol;
    }

    public int getPriority() {
        return priority;
    }

    public double apply(Operand first, Operand second) {
        return apply(first.getValue(), second.getValue());
    }

    public double apply(double first, double second) {
        return solver.applyAsDouble(first, second);
    }

    @Override
    public String toString() {
        return "Operator(" + name().toLowerCase() + ')';
    }
}
