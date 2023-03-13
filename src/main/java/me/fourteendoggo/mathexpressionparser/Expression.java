package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.container.TokenList;
import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.tokens.Operand;
import me.fourteendoggo.mathexpressionparser.tokens.Operator;
import me.fourteendoggo.mathexpressionparser.tokens.Token;
import me.fourteendoggo.mathexpressionparser.tokens.TokenType;

import java.util.function.IntPredicate;

public class Expression {
    private final TokenList tokens;
    private final Tokenizer tokenizer;

    public Expression(char[] input) {
        this(input, current -> true);
    }

    Expression(char[] input, IntPredicate loopCondition) {
        this.tokens = new TokenList();
        this.tokenizer = new Tokenizer(input, loopCondition);
    }

    Tokenizer getTokenizer() {
        return tokenizer;
    }

    public double parse() {
        tokenizer.forEachRemaining(current -> {
            switch (current) {
                case ' ', '\t', '\r', '\n' -> tokenizer.next();
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> pushOperand(current, false);
                case '^' -> pushToken(Operator.POWER);
                case '*' -> pushToken(Operator.MULTIPLICATION);
                case '/' -> pushToken(Operator.DIVISION);
                case '%' -> pushToken(Operator.MODULO);
                case '+' -> pushToken(Operator.ADDITION);
                case '-' -> {
                    switch (tokens.getLastType()) {
                        case OPERAND -> pushToken(Operator.SUBTRACTION);
                        case OPERATOR -> pushOperand(Tokenizer.NULL_CHAR, true);
                        default -> throw new SyntaxException("invalid position for negative sign");
                    }
                }
                case '(' -> {
                    // replace things like 2(3+4) with 2*(3+4)
                    pushMultiplicationIfNeeded();
                    pushToken(tokenizer.readBrackets());
                }
                case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' -> {
                    // replace things like 2cos(3) with 2*cos(3)
                    pushMultiplicationIfNeeded();
                    tokens.pushToken(tokenizer.readFunction());
                }
                default -> throw new SyntaxException("unexpected character: " + current);
            }
        });
        return tokens.solve();
    }

    private void pushMultiplicationIfNeeded() {
        if (tokens.getLastType() == TokenType.OPERAND) {
            tokens.pushToken(Operator.MULTIPLICATION);
        }
    }

    private void pushOperand(char firstDigit, boolean negative) {
        tokenizer.next(); // already read first digit
        Operand operand = tokenizer.readOperand(firstDigit, negative);
        tokens.pushToken(operand);
    }

    private void pushToken(Token token) {
        // order of calls doesn't matter
        tokenizer.next();
        tokens.pushToken(token);
    }
}
