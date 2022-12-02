package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.tokens.Operand;
import me.fourteendoggo.mathexpressionparser.tokens.Operator;

import java.util.function.IntPredicate;

public class Expression {
    private final char[] input;
    private final TokenChain tokens;
    private final TokenReader reader;
    private final IntPredicate loopCondition;

    public Expression(char[] input) {
        this(input, current -> true);
    }

    public Expression(char[] input, IntPredicate loopCondition) {
        this.input = input;
        this.tokens = new TokenChain();
        this.reader = new TokenReader(input);
        this.loopCondition = loopCondition;
    }

    public double parse() {
        char currentChar;
        while (reader.hasRemaining() && loopCondition.test((currentChar = reader.current()))) {

            if (Character.isWhitespace(currentChar)) {
                reader.get(); // increment position or we keep looping over whitespaces
                continue;
            }

            switch (currentChar) {
                case '-' -> {
                    TokenType lastType = tokens.getLastType();
                    if (lastType == null || lastType == TokenType.OPERATOR) {
                        // this is a negative sign, let the buffer start reading from here
                        appendOperand();
                    } else if (lastType == TokenType.OPERAND) {
                        appendToken(Operator.SUBTRACTION);
                    } else {
                        throw new SyntaxException("invalid position for negative sign");
                    }
                }
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> appendOperand();
                case '^' -> appendToken(Operator.POWER);
                case '*' -> appendToken(Operator.MULTIPLICATION);
                case '/' -> appendToken(Operator.DIVISION);
                case '%' -> appendToken(Operator.MODULO);
                case '+' -> appendToken(Operator.ADDITION);
                case '(' -> {
                    tokens.validateIncomingType(TokenType.LEFT_PARENTHESIS);
                    Expression sub = new Expression(input, current -> current != ')');
                    sub.reader.position(reader.position() + 1); // modify their position to start reading behind the '('
                    appendToken(new Operand(sub.parse()));
                    reader.position(sub.reader.position() + 1); // modify our position to start reading behind the ')'
                }
                default -> throw new SyntaxException("invalid character: " + currentChar);
            }
        }
        return tokens.solve();
    }

    private void appendOperand() {
        double readDouble = reader.readDouble();
        tokens.addToken(new Operand(readDouble));
    }

    private void appendToken(Token token) {
        tokens.addToken(token);
        reader.get(); // move to the next char, #appendOperand does this automatically!
    }
}
