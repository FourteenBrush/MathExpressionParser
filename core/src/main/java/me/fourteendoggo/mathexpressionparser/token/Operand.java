package me.fourteendoggo.mathexpressionparser.token;

/**
 * A literal number, wrapped to be processed.
 */
public class Operand implements Token {
    private double value;

    public Operand(double value) {
        this.value = value;
    }

    @Override
    public TokenType getType() {
        return TokenType.OPERAND;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
