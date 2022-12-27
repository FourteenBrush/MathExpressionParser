package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.tokens.Operand;
import me.fourteendoggo.mathexpressionparser.tokens.Operator;

import java.util.function.IntPredicate;

public class Expression {
    private final char[] input;
    private final TokenList tokens;
    private final Tokenizer tokenizer;
    private final IntPredicate loopCondition;

    public Expression(char[] input) {
        this(input, current -> true);
    }

    public Expression(char[] input, IntPredicate loopCondition) {
        this.input = input;
        this.tokens = new TokenList();
        this.tokenizer = new Tokenizer(input);
        this.loopCondition = loopCondition;
    }

    Tokenizer getTokenizer() { // package-private
        return tokenizer;
    }

    public double parse() {
        char currentChar;
        while (tokenizer.hasRemaining() && loopCondition.test((currentChar = tokenizer.current()))) {

            if (Character.isWhitespace(currentChar)) {
                tokenizer.next(); // increment position or we keep looping over whitespaces
                continue;
            }

            switch (currentChar) {
                case '-' -> {
                    TokenType lastType = tokens.getLastType();
                    if (lastType == null || lastType == TokenType.OPERATOR) {
                        // this is a negative sign, read a negative operand
                        appendOperand();
                    } else if (lastType == TokenType.OPERAND) {
                        appendToken(Operator.SUBTRACTION);
                    } else {
                        throw new SyntaxException("invalid position for negative sign");
                    }
                }
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> appendOperand(currentChar);
                case '^' -> appendToken(Operator.POWER);
                case '*' -> appendToken(Operator.MULTIPLICATION);
                case '/' -> appendToken(Operator.DIVISION);
                case '%' -> appendToken(Operator.MODULO);
                case '+' -> appendToken(Operator.ADDITION);
                case '(' -> {
                    // TODO clean up
                    if (tokens.getLastType() == TokenType.OPERAND) {
                        // replace things like 2(3+4) with 2*(3+4)
                        tokens.addToken(Operator.MULTIPLICATION);
                    }
                    tokens.validateIncomingType(TokenType.LEFT_PARENTHESIS);
                    // sub will continue reading until it finds a right parenthesis
                    Expression sub = new Expression(input, current -> current != ')');
                    sub.tokenizer.position(tokenizer.position() + 1); // modify their position to start reading behind the '('
                    appendToken(new Operand(sub.parse())); // sub.reader pos is now at the ')'
                    Assert.isTrue(sub.tokenizer.hasRemaining() && sub.tokenizer.current() == ')', "missing closing parenthesis");
                    tokenizer.position(sub.tokenizer.position() + 1); // modify our position to resume reading behind the ')'
                }
                case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' -> {
                    if (tokens.getLastType() == TokenType.OPERAND) {
                        // replace things like 2cos(3) with 2*cos(3)
                        tokens.addToken(Operator.MULTIPLICATION);
                    }
                    Operand function = tokenizer.readFunction();
                    tokens.addToken(function);
                }
                default -> throw new SyntaxException("invalid character: " + currentChar);
            }
        }
        return tokens.solve();
    }

    private void appendOperand() {
        Operand operand = tokenizer.readOperand();
        tokens.addToken(operand);
    }

    private void appendOperand(char firstChar) {
        tokenizer.next(); // skip char
        Operand operand = tokenizer.readOperand(firstChar);
        tokens.addToken(operand);
    }

    private void appendToken(Token token) {
        tokens.addToken(token);
        tokenizer.next();
    }
}
