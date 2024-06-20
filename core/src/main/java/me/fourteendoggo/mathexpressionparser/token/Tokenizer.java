package me.fourteendoggo.mathexpressionparser.token;

import me.fourteendoggo.mathexpressionparser.symbol.ExecutionEnv;
import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.function.FunctionCallSite;
import me.fourteendoggo.mathexpressionparser.function.FunctionContext;
import me.fourteendoggo.mathexpressionparser.symbol.Symbol;
import me.fourteendoggo.mathexpressionparser.symbol.Variable;
import me.fourteendoggo.mathexpressionparser.utils.Assert;
import me.fourteendoggo.mathexpressionparser.utils.Utility;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.IntPredicate;

public class Tokenizer {
    private final char[] source;
    private final ExecutionEnv env;
    private final Expression expr = new Expression();
    // indicates whether we should keep processing characters
    // e.g. to implement sub-tokenizers
    private final IntPredicate loopCondition;
    private int pos;

    public Tokenizer(char[] source, ExecutionEnv env) {
        this(source, env, current -> true);
    }

    public Tokenizer(char[] source, ExecutionEnv env, IntPredicate loopCondition) {
        this.source = source;
        this.env = env;
        this.loopCondition = loopCondition;
    }

    /*
    NOTE: usage of x_2 where x_ is a variable is not supported, if you expect x_*2, specify it explicitly
     */

    public Expression readTokens() {
        while (hasRemaining()) {
            char current = advance();
            switch (current) {
                case ' ', '\r', '\t' -> {} // no-op
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> pushOperand(current);
                case '*' -> expr.pushToken(Operator.MULTIPLICATION);
                case '/' -> expr.pushToken(Operator.DIVISION);
                case '+' -> expr.pushToken(Operator.ADDITION);
                case '%' -> expr.pushToken(Operator.MODULO);
                // TODO: revert back pow() and ^ design decision
                case '^' -> {
                    // one of the highest priority operators, can be solved immediately
                    // TODO: solve this here rather than pushing an Operator
                    expr.pushToken(Operator.POWER);
                }
                case '-' -> {
                    switch (expr.getLastType()) {
                        case OPERAND -> expr.pushToken(Operator.SUBTRACTION);
                        case OPERATOR -> pushNegativeOperand();
                    }
                }
                case '<' -> {
                    switch (advanceOrThrow()) {
                        case '<' -> expr.pushToken(Operator.LEFT_SHIFT);
                        case '=' -> expr.pushToken(Operator.LESS_THAN_OR_EQUAL);
                        default -> {
                            expr.pushToken(Operator.LESS_THAN);
                            pos--; // put the character after < back
                        }
                    }
                }
                case '>' -> {
                    switch (advanceOrThrow()) {
                        case '>' -> expr.pushToken(Operator.RIGHT_SHIFT);
                        case '=' -> expr.pushToken(Operator.GREATER_THAN_OR_EQUAL);
                        default -> {
                            expr.pushToken(Operator.GREATER_THAN);
                            pos--; // put the character after > back
                        }
                    }
                }
                case '=' -> {
                    matchOrThrow('=', "expected another '=' for comparison");
                    expr.pushToken(Operator.EQUALS);
                }
                case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                     'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '_' -> {
                    // support for things like 2cos(1) -> 2 * cos(1)
                    if (expr.getLastType() == TokenType.OPERAND) {
                        expr.pushToken(Operator.MULTIPLICATION);
                    }
                    expr.pushToken(readSymbol());
                }
                case '&' -> {
                    if (match('&')) { // already standing on the second '&' then
                        expr.pushToken(Operator.LOGICAL_AND);
                    } else {
                        expr.pushToken(Operator.BITWISE_AND);
                    }
                }
                case '|' -> {
                    if (match('|')) {
                        expr.pushToken(Operator.LOGICAL_OR);
                    } else {
                        expr.pushToken(Operator.BITWISE_OR);
                    }
                }
                case '(' -> {
                    // support for things like 2(1 + 1) -> 2 * (1 + 1)
                    if (expr.getLastType() == TokenType.OPERAND) {
                        expr.pushToken(Operator.MULTIPLICATION);
                    }
                    expr.pushToken(readBrackets());
                }
                case '!' -> {
                    if (currentOrThrow("expected an operand") == '=') {
                        advance();
                        expr.pushToken(Operator.NOT_EQUALS);
                    } else { // one of the highest priority operators, can be solved immediately
                        // TODO: outline
                        Tokenizer tokenizer = branchOff(loopCondition, pos);
                        double toBeNegated = tokenizer.readTokens().solve();
                        pos = tokenizer.pos;
                        expr.pushToken(Utility.boolNot(toBeNegated));
                    }
                }
                case '~' -> { // one of the highest priority operators, can be solved immediately
                    // TODO: outline
                    Tokenizer tokenizer = branchOff(loopCondition, pos);
                    int input = Utility.requireInt(tokenizer.readTokens().solve());
                    pos = tokenizer.pos;
                    expr.pushToken(~input);
                }
                default -> throw new SyntaxException("unexpected character " + current);
            }
        }
        return expr;
    }

    private void pushNegativeOperand() {
        double value = -readDouble('0', false);
        expr.pushToken(value);
    }

    private void pushOperand(char initialChar) {
        double value = readDouble(initialChar, true);
        expr.pushToken(value);
    }

    /**
     * Reads a double starting at the current pos
     * @param initialChar the first char of the number, or '0' if this was a negative number.
     *                    If this was a negative number, {@code readNumber} is also set to false.
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
            Assert.isTrue(Utility.isValidIdentifierFirstChar(currentOrDefault()), "expected a number");
            return 1; // pushNegativeOperand() inverts this, so we're actually returning -1
        }
        return result;
    }

    // TODO: fully implement
    /**
     * Reads a double from the input buffer.
     * @param initialChar the first char of the number, or '0' if this was a negative number.
     * @param negative false to indicate that the initialChar was actually a negative sign.
     * @return the read double
     * @throws SyntaxException if the buffer contains some malformed double
     * <p>
     * Examples:
     * <p>
     * <ul>
     *     <li>"0" -> readDouble('0', true)</li>
     *     <li>"-0" -> readDouble('0', false)</li>
     *     <li>"123" -> readDouble('1', true)</li>
     * </ul>
     */
    @ApiStatus.Experimental
    private double readDoubleExperimental(char initialChar, boolean negative) {
        // potentialBase becomes true if we have read 0 or -0
        // [-]0<potential base>
        boolean potentialBase = (negative && initialChar == '0') || match('0');

        int base = potentialBase ? switch (currentOrDefault()) {
            case 'b', 'B' -> { pos++; yield 2; }
            case 'o', 'O' -> { pos++; yield 8; }
            case 'x', 'X' -> { pos++; yield 16; }
            // NOTE: this is basically a catch for '\0', we wouldn't be here
            // if currentOrDefault() was anything else
            // TODO: can't we return 0 immediately here, instead of an extra branch?
            default -> 0; // -0 or 0, end of input
        } : 10;

        System.out.println("initial: " + initialChar);
        System.out.println("negative: " + negative);
        System.out.println("standing on " + currentOrDefault());
        System.out.println("base: " + base);

        if (base == 0) return 0; // -0 or 0, end of input

        // if we have read a base, then expect a number
        double result = initialChar - '0';

        loop:
        while (pos < source.length) {
            char current = advance();
            switch (current) {
                // still waiting for range syntax in java
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                     'a', 'b', 'c', 'd', 'e', 'f',
                     'A', 'B', 'C', 'D', 'E', 'F' -> {
                    // NOTE: a letter could also mean a function name is following
                    // TODO: lets imagine 0xfunction(), how can we know where the func name starts?
                    if (!isValidCharForBase(current, base)) {
                        current |= ' '; // set lowercase bit
                        if (current >= 'a' && current <= 'z') {
                            // assume a function follows
                            pos--; // put the letter back
                            break loop;
                        }
                    }
                }
            }
            System.out.print(current);
        }
        System.out.println();
        return result;
    }

    private static boolean isValidCharForBase(char c, int base) {
        return switch (base) {
            case 2 -> c == '0' || c == '1';
            case 8 -> c >= '0' && c <= '7';
            case 10 -> c >= '0' && c <= '9';
            case 16 -> (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
            default -> false;
        };
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

    private double readBrackets() {
        Tokenizer tokenizer = branchOff(c -> c != ')', pos); // enter expression
        double result = tokenizer.readTokens().solve();

        Assert.isTrue(tokenizer.currentOrDefault() == ')', "missing closing parenthesis");
        pos = tokenizer.pos + 1;
        return result;
    }

    private Operand readSymbol() {
        Symbol symbol = env.lookupSymbol(source, pos - 1); // already incremented pos
        pos += symbol.getName().length() - 1;

        // TODO: change this to switch (symbol), whatever language version that may be
        return switch (symbol.getType()) {
            case FUNCTION -> readFunctionCall((FunctionCallSite) symbol);
            case VARIABLE -> new Operand(((Variable) symbol).value());
        };
    }

    private Operand readFunctionCall(FunctionCallSite desc) {
        String functionName = desc.getName();
        matchOrThrow('(', "missing opening parenthesis for function %s", functionName);

        char maybeClosingParenthesis = currentOrThrow("missing closing parenthesis for function call %s", functionName);
        FunctionContext parameters = desc.allocateParameters();

        if (maybeClosingParenthesis != ')') { // parameters were provided
            // TODO: when calling f.e. exit( ), the space gets interpreted as parameters too
            Assert.isTrue(desc.supportsArgs(), "function %s did not expect any parameters", functionName);

            // TODO: can we reuse the tokenizer and avoid an extra allocation per parameter?
            Tokenizer paramTokenizer = this;
            do {
                paramTokenizer = paramTokenizer.branchOff(Utility::isValidArgument, paramTokenizer.pos);
                parameters.add(paramTokenizer.readTokens().solve());
            } while (paramTokenizer.match(','));
            pos = paramTokenizer.pos; // move to ')' (or something else if there's an error)
        }
        matchOrThrow(')', "missing closing parenthesis for function %s", functionName);

        return new Operand(desc.apply(parameters));
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

    private char currentOrThrow(String fmt, Object... placeholders) {
        Assert.isTrue(pos < source.length, fmt, placeholders);
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

    private void matchOrThrow(char expected, String fmt, Object... placeholders) {
        Assert.isTrue(match(expected), fmt, placeholders);
    }

    private Tokenizer branchOff(IntPredicate newLoopCondition, int newPos) {
        Tokenizer tokenizer = new Tokenizer(source, env, newLoopCondition);
        tokenizer.pos = newPos;
        return tokenizer;
    }
}
