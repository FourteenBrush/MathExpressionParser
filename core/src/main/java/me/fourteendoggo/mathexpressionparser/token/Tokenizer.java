package me.fourteendoggo.mathexpressionparser.token;

import me.fourteendoggo.mathexpressionparser.environment.ExecutionEnv;
import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.function.FunctionCallSite;
import me.fourteendoggo.mathexpressionparser.function.FunctionContext;
import me.fourteendoggo.mathexpressionparser.symbol.Symbol;
import me.fourteendoggo.mathexpressionparser.symbol.Variable;
import me.fourteendoggo.mathexpressionparser.utils.Assert;
import me.fourteendoggo.mathexpressionparser.utils.Utility;

import java.util.function.IntPredicate;

public class Tokenizer {
    private final char[] source;
    private final ExecutionEnv env;
    private final TokenList tokens;
    private final IntPredicate loopCondition;
    private int pos;

    public Tokenizer(char[] source, ExecutionEnv env) {
        this(source, env, current -> true);
    }

    public Tokenizer(char[] source, ExecutionEnv env, IntPredicate loopCondition) {
        this.source = source;
        this.env = env;
        this.loopCondition = loopCondition;
        this.tokens = new TokenList();
    }

    public TokenList readTokens() {
        while (hasRemaining()) {
            char current = advance();
            switch (current) {
                case ' ', '\r', '\t' -> {} // no-op
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> pushOperand(current);
                case '*' -> tokens.pushToken(Operator.MULTIPLICATION);
                case '/' -> tokens.pushToken(Operator.DIVISION);
                case '+' -> tokens.pushToken(Operator.ADDITION);
                case '%' -> tokens.pushToken(Operator.MODULO);
                case '^' -> tokens.pushToken(Operator.BITWISE_XOR);
                case '-' -> {
                    switch (tokens.getLastType()) {
                        case OPERAND -> tokens.pushToken(Operator.SUBTRACTION);
                        case OPERATOR -> pushNegativeOperand();
                    }
                }
                case '<' -> {
                    switch (advanceOrThrow()) {
                        case '<' -> tokens.pushToken(Operator.LEFT_SHIFT);
                        case '=' -> tokens.pushToken(Operator.LESS_THEN_OR_EQUAL);
                        default -> {
                            tokens.pushToken(Operator.LESS_THEN);
                            pos--; // put the character after < back
                        }
                    }
                }
                case '>' -> {
                    switch (advanceOrThrow()) {
                        case '>' -> tokens.pushToken(Operator.RIGHT_SHIFT);
                        case '=' -> tokens.pushToken(Operator.GREATER_THEN_OR_EQUAL);
                        default -> {
                            tokens.pushToken(Operator.GREATER_THEN);
                            pos--; // put the character after > back
                        }
                    }
                }
                case '=' -> {
                    matchOrThrow('=', "expected another '=' for comparison");
                    tokens.pushToken(Operator.EQUALS);
                }
                case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                     'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' -> {
                    // support for things like 2cos(1) -> 2 * cos(1)
                    if (tokens.getLastType() == TokenType.OPERAND) {
                        tokens.pushToken(Operator.MULTIPLICATION);
                    }
                    tokens.pushToken(readSymbol());
                    // tokens.pushToken(readFunctionCall0());
                }
                case '&' -> {
                    if (match('&')) { // already standing on the second '&' then
                        tokens.pushToken(Operator.LOGICAL_AND);
                    } else {
                        tokens.pushToken(Operator.BITWISE_AND);
                    }
                }
                case '|' -> {
                    if (match('|')) {
                        tokens.pushToken(Operator.LOGICAL_OR);
                    } else {
                        tokens.pushToken(Operator.BITWISE_OR);
                    }
                }
                case '(' -> {
                    // support for things like 2(1 + 1) -> 2 * (1 + 1)
                    if (tokens.getLastType() == TokenType.OPERAND) {
                        tokens.pushToken(Operator.MULTIPLICATION);
                    }
                    tokens.pushToken(readBrackets());
                }
                case '!' -> {
                    if (currentOrThrow("expected an operand") == '=') {
                        advance();
                        tokens.pushToken(Operator.NOT_EQUALS);
                    } else { // one of the highest priority operators, can be solved immediately
                        Tokenizer tokenizer = branchOff(loopCondition, pos);
                        double toBeNegated = tokenizer.readTokens().solve();
                        pos = tokenizer.pos;
                        tokens.pushToken(new Operand(Utility.boolNot(toBeNegated)));
                    }
                }
                case '~' -> { // one of the highest priority operators, can be solved immediately
                    Tokenizer tokenizer = branchOff(loopCondition, pos);
                    int input = Utility.requireInt(tokenizer.readTokens().solve());
                    pos = tokenizer.pos;
                    tokens.pushToken(new Operand(~input));
                }
                default -> throw new SyntaxException("unexpected character " + current);
            }
        }
        return tokens;
    }

    private boolean hasRemaining() {
        return pos < source.length && loopCondition.test(source[pos]);
    }

    private char advance() {
        return source[pos++];
    }

    private char advanceOrThrow() {
        Assert.isTrue(hasRemaining(), "expected an operand");
        return advance();
    }

    private char currentOrThrow(String message) {
        Assert.isTrue(pos < source.length, message);
        return source[pos];
    }

    private char currentOrDefault() {
        if (pos < source.length) {
            return source[pos];
        }
        return '\0';
    }

    private boolean match(char c) {
        if (pos >= source.length || source[pos] != c) {
            return false;
        }
        pos++;
        return true;
    }

    private void matchOrThrow(char expected, String message) {
        Assert.isTrue(match(expected), message);
    }

    private Tokenizer branchOff(IntPredicate newLoopCondition, int newPos) {
        Tokenizer tokenizer = new Tokenizer(source, env, newLoopCondition);
        tokenizer.pos = newPos;
        return tokenizer;
    }

    private void pushNegativeOperand() {
        double value = -readDouble('0', false);
        tokens.pushToken(new Operand(value));
    }
    
    private void pushOperand(char initialChar) {
        double value = readDouble(initialChar, true);
        tokens.pushToken(new Operand(value));
    }

    /**
     * Reads a double starting at the current pos
     * @param initialChar the first char of the number, or '0' if this was a negative sign
     * @param readNumber whether the initialChar was actually part of the number, false if it accounts for a negative sign
     * @return the read double
     * @throws SyntaxException if the buffer contains some malformed double
     */
    private double readDouble(char initialChar, boolean readNumber) {
        double result = initialChar - '0';

        loop:
        while (pos < source.length) {
            char current = advance();
            switch (current) {
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                    result *= 10;
                    result += current - '0';
                    readNumber = true;
                }
                case '.' -> {
                    Assert.isTrue(readNumber, "expected a number before the comma");
                    double decimalPart = readDecimalPart();
                    result += decimalPart;
                    return result;
                }
                default -> {
                    pos--; // read too far then
                    break loop;
                }
            }
        }
        if (!readNumber) {
            // support for function calls of form -func()
            Assert.isTrue(Utility.isLowercaseLetter(currentOrDefault()), "expected a number");
            return 1; // pushNegativeOperand() inverts this, so we're actually returning -1
        }
        return result;
    }

    private double readDecimalPart() {
        int oldPos = pos;
        double result = 0;
        double divider = 10; // always power of ten

        loop:
        while (pos < source.length) {
            char current = source[pos];
            switch (current) {
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                    result += (current - '0') / divider;
                    divider *= 10;
                    pos++;
                }
                default -> { break loop; }
            }
        }
        Assert.isTrue(pos > oldPos, "expected the decimal part of a number");
        return result;
    }

    private Operand readBrackets() {
        Tokenizer tokenizer = branchOff(Utility::isBetweenBrackets, pos); // enter expression
        Operand result = new Operand(tokenizer.readTokens().solve());

        Assert.isTrue(tokenizer.currentOrDefault() == ')', "missing closing parenthesis");
        pos = tokenizer.pos + 1;
        return result;
    }

    private Operand readSymbol() {
        Symbol symbol = env.lookupSymbol(source, pos - 1); // already incremented pos
        pos += symbol.getName().length() - 1;

        // TODO: change this to java 19 switch, want to keep the project on java 17 for now
        return switch (symbol.getType()) {
            case FUNCTION -> readFunctionCall((FunctionCallSite) symbol);
            case VARIABLE -> new Operand(((Variable) symbol).value());
        };
    }

    private Operand readFunctionCall(FunctionCallSite function) {
        matchOrThrow('(', "missing opening parenthesis for function call");

        FunctionContext parameters = function.allocateParameters();
        char maybeClosingParenthesis = currentOrThrow("missing closing parenthesis for function call " + function.getName());
        if (maybeClosingParenthesis != ')') { // arguments were provided
            // TODO: when calling f.e. exit( ), the space gets interpreted as parameters too
            Assert.isTrue(function.supportsArgs(), "did not expect any parameters for function %s", function.getName());

            // do while doesn't really work here due to that + 1
            Tokenizer paramTokenizer = branchOff(Utility::isValidArgument, pos);
            parameters.add(paramTokenizer.readTokens().solve());

            while (paramTokenizer.currentOrDefault() == ',') {
                paramTokenizer = paramTokenizer.branchOff(Utility::isValidArgument, paramTokenizer.pos + 1); // + 1 to consume comma
                parameters.add(paramTokenizer.readTokens().solve());
            }
            pos = paramTokenizer.pos; // move to ')' (or something else if there's an error)
        }
        matchOrThrow(')', "missing closing parenthesis for function call " + function.getName());

        return new Operand(function.apply(parameters));
    }
}