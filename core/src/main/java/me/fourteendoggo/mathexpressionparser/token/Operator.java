package me.fourteendoggo.mathexpressionparser.token;

import java.util.function.DoubleBinaryOperator;

import static me.fourteendoggo.mathexpressionparser.utils.Utility.*;

public enum Operator implements Token {
    POWER("^", 32, Math::pow),
    MULTIPLICATION("*", 29, (a, b) -> a * b),
    DIVISION("/", 29, (a, b) -> a / b),
    MODULO("%", 29, (a, b) -> a % b),
    ADDITION("+", 28, Double::sum),
    SUBTRACTION("-", 28, (a, b) -> a - b),
    LEFT_SHIFT("<<", 27, (a, b) -> requireInt(a) << requireInt(b)),
    RIGHT_SHIFT(">>", 27, (a, b) -> requireInt(a) >> requireInt(b)),
    LESS_THAN("<", 25, (a, b) -> boolToDouble(a < b)),
    GREATER_THAN(">", 25, (a, b) -> boolToDouble(a > b)),
    LESS_THAN_OR_EQUAL("<=", 25, (a, b) -> boolToDouble(a <= b)),
    GREATER_THAN_OR_EQUAL(">=", 25, (a, b) -> boolToDouble(a >= b)),
    EQUALS("==", 24, (a, b) -> boolToDouble(a == b)),
    NOT_EQUALS("!=", 24, (a, b) -> boolToDouble(a != b)),
    BITWISE_AND("&", 23, (a, b) -> requireInt(a) & requireInt(b)),
    //BITWISE_XOR("^", 22, (a, b) -> requireInt(a) ^ requireInt(b)),
    BITWISE_OR("|", 21, (a, b) -> requireInt(a) | requireInt(b)),
    LOGICAL_AND("&&", 20, (a, b) -> boolToDouble(doubleToBool(a) && doubleToBool(b))),
    LOGICAL_OR("||", 19, (a, b) -> boolToDouble(doubleToBool(a) || doubleToBool(b)));

    public static final int HIGHEST_PRIORITY;
    private final String symbol;
    private final int priority;
    private final DoubleBinaryOperator func;

    static {
        int highestPriority = 0;
        for (Operator operator : values()) {
            highestPriority = Math.max(highestPriority, operator.priority);
        }
        HIGHEST_PRIORITY = highestPriority;
    }

    Operator(String symbol, int priority, DoubleBinaryOperator func) {
        this.symbol = symbol;
        this.priority = priority;
        this.func = func;
    }

    @Override
    public TokenType getType() {
        return TokenType.OPERATOR;
    }

    /**
     * @return the symbol representing this operator
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * @return the priority of the operator, between 1 and {@link #HIGHEST_PRIORITY}, higher priority will be executed first
     */
    public int getPriority() {
        return priority;
    }

    public double apply(Operand first, Operand second) {
        return func.applyAsDouble(first.getValue(), second.getValue());
    }

    public double apply(double first, double second) {
        return func.applyAsDouble(first, second);
    }

    @Override
    public String toString() {
        return symbol;
    }
}
