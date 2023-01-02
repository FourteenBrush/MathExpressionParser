package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.container.TokenList;
import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.tokens.Operand;
import me.fourteendoggo.mathexpressionparser.tokens.Operator;

import java.util.function.IntPredicate;

public class Expression {
    private final TokenList tokens;
    private final Tokenizer tokenizer;

    public Expression(char[] input) {
        this(input, c -> true);
    }

    public Expression(char[] input, IntPredicate loopCondition) {
        this.tokens = new TokenList();
        this.tokenizer = new Tokenizer(input, loopCondition);
    }

    Tokenizer getTokenizer() { // package-private
        return tokenizer;
    }

    public double parse() {
        while (tokenizer.hasRemaining()) {
            char currentChar = tokenizer.current();

            if (Character.isWhitespace(currentChar)) {
                tokenizer.next(); // increment position or we keep looping over whitespaces
                continue;
            }

            switch (currentChar) {
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> appendOperand(currentChar, false);
                case '^' -> appendToken(Operator.POWER);
                case '*' -> appendToken(Operator.MULTIPLICATION);
                case '/' -> appendToken(Operator.DIVISION);
                case '%' -> appendToken(Operator.MODULO);
                case '+' -> appendToken(Operator.ADDITION);
                case '-' -> {
                    TokenType lastType = tokens.getLastType();
                    if (lastType == null || lastType == TokenType.OPERATOR) {
                        appendOperand('0', true);
                    } else if (lastType == TokenType.OPERAND) {
                        appendToken(Operator.SUBTRACTION);
                    } else {
                        throw new SyntaxException("invalid position for negative sign");
                    }
                }
                case '(' -> {
                    // replace things like 2(3+4) with 2*(3+4)
                    appendMultiplicationIfNecessary();
                    appendToken(tokenizer.readBrackets());
                }
                case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                     'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' -> {
                    // replace things like 2cos(3) with 2*cos(3)
                    appendMultiplicationIfNecessary();
                    tokens.addToken(tokenizer.readFunction());
                }
                default -> throw new SyntaxException("invalid character: " + currentChar);
            }
        }
        return tokens.solve();
    }

    private void appendMultiplicationIfNecessary() {
        if (tokens.getLastType() == TokenType.OPERAND) {
            appendToken(Operator.MULTIPLICATION);
        }
    }

    private void appendOperand(char firstNumber, boolean negative) {
        tokenizer.next();
        Operand operand = tokenizer.readOperand(firstNumber, negative);
        tokens.addToken(operand);
    }

    private void appendToken(Token token) {
        tokenizer.next();
        tokens.addToken(token);
    }
}
