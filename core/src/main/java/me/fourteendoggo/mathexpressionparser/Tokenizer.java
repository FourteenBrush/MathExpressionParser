package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.container.TokenList;
import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.function.FunctionCallSite;
import me.fourteendoggo.mathexpressionparser.function.FunctionContainer;
import me.fourteendoggo.mathexpressionparser.function.FunctionContext;
import me.fourteendoggo.mathexpressionparser.tokens.Operand;
import me.fourteendoggo.mathexpressionparser.tokens.Operator;
import me.fourteendoggo.mathexpressionparser.tokens.Token;
import me.fourteendoggo.mathexpressionparser.tokens.TokenType;
import me.fourteendoggo.mathexpressionparser.utils.Assert;
import me.fourteendoggo.mathexpressionparser.utils.Utility;

import java.util.function.IntPredicate;

public class Tokenizer {
    private static final char NULL_CHAR = '\0';
    private static final FunctionContainer FUNCTION_CONTAINER = FunctionContainer.withDefaultFunctions();
    private final char[] source;
    private final IntPredicate condition;
    private final TokenList tokens;
    private int pos;

    public Tokenizer(char[] source) {
        this(source, current -> true);
    }

    public Tokenizer(char[] source, IntPredicate condition) {
        this.source = source;
        this.condition = condition;
        this.tokens = new TokenList();
    }

    static FunctionContainer getFunctionContainer() {
        return FUNCTION_CONTAINER;
    }

    private char current() {
        return source[pos];
    }

    private void advance() {
        pos++;
    }

    private char peek() {
        if (pos >= source.length) return NULL_CHAR;
        return source[pos];
    }

    private void position(int pos) {
        this.pos = pos;
    }

    private boolean hasAndGet(char expected) {
        if (peek() != expected) {
            return false;
        }
        advance();
        return true;
    }

    private void expectAndGet(char expected, String exceptionMessage) {
        Assert.isTrue(hasAndGet(expected), exceptionMessage);
    }

    private Tokenizer branchOff(IntPredicate newCondition, int newPos) {
        Tokenizer res = new Tokenizer(source, newCondition);
        res.position(newPos);
        return res;
    }

    public TokenList readTokens() {
        while (pos < source.length && condition.test(source[pos])) {
            char current = source[pos];
            switch (current) {
                case ' ', '\r', '\t' -> advance();
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> pushOperand(current, false);
                case '^' -> pushToken(Operator.POWER);
                case '*' -> pushToken(Operator.MULTIPLICATION);
                case '/' -> pushToken(Operator.DIVISION);
                case '%' -> pushToken(Operator.MODULO);
                case '+' -> pushToken(Operator.ADDITION);
                case '-' -> {
                    switch (tokens.getLastType()) {
                        case OPERAND -> pushToken(Operator.SUBTRACTION);
                        case OPERATOR -> pushOperand(NULL_CHAR, true);
                        default -> throw new SyntaxException("invalid position for a negative sign");
                    }
                }
                case '(' -> {
                    // replace things like 2(3+4) with 2*(3+4)
                    pushMultiplicationIfNeeded();
                    pushToken(readBrackets());
                }
                case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                     'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' -> {
                    // replace things like 2cos(3) with 2*cos(3)
                    pushMultiplicationIfNeeded();
                    tokens.pushToken(readFunctionCall());
                }
                default -> throw new SyntaxException("unexpected character: " + current);
            }
        }
        return tokens;
    }

    private void pushOperand(char firstDigit, boolean negative) {
        advance(); // we already read the first digit
        tokens.pushToken(readOperand(firstDigit, negative));
    }

    private void pushToken(Token token) {
        advance(); // assuming token is only one char
        tokens.pushToken(token);
    }

    private void pushMultiplicationIfNeeded() {
        if (tokens.getLastType() == TokenType.OPERAND) {
            tokens.pushToken(Operator.MULTIPLICATION);
        }
    }

    private Operand readBrackets() {
        Tokenizer tokenizer = branchOff(Utility::isBetweenBrackets, pos + 1); // enter expression
        Operand result = new Operand(tokenizer.readTokens().solve());
        pos = tokenizer.pos;
        Assert.isTrue(peek() == ')', "missing closing parenthesis");
        return result;
    }

    private Operand readFunctionCall() {
        FunctionCallSite function = FUNCTION_CONTAINER.getFunction(source, pos);
        pos += function.getName().length(); // skip function name
        expectAndGet('(', "missing opening parenthesis for function call");

        FunctionContext parameters = function.allocateParameters();
        // only read parameters if the function supports it or if there are optional parameters filled in
        // if current points to anything other than a closing parenthesis, we know we got a parameter
        if (peek() != ')') {
            Assert.isTrue(function.supportsArgs(), "did not expect any parameters for function %s", function.getName());
            Tokenizer paramTokenizer = new Tokenizer(source, Utility::isValidArgument);
            paramTokenizer.position(pos); // position them to read the first parameter

            parameters.add(paramTokenizer.readTokens().solve());
            while (paramTokenizer.peek() == ',') { // extra param coming
                int oldPos = paramTokenizer.pos;
                paramTokenizer = paramTokenizer.branchOff(Utility::isValidArgument, oldPos + 1);
                parameters.add(paramTokenizer.readTokens().solve());
            }
            pos = paramTokenizer.pos;
        }
        expectAndGet(')', "missing closing parenthesis");

        return new Operand(function.apply(parameters));
    }

    private Operand readOperand(char firstDigit, boolean negative) {
        return new Operand(readDouble(firstDigit, negative));
    }

    private double readDouble(char firstDigit, boolean negative) {
        IntResult beforeComma = readInt(firstDigit, negative);
        if (!hasAndGet('.')) { // skips decimal point if present
            beforeComma.alignState();
            return beforeComma.getValue();
        }
        double decimalPart = readDecimalPart();
        beforeComma.alignState();

        if (beforeComma.negative) {
            return beforeComma.getValue() - decimalPart;
        }
        return beforeComma.getValue() + decimalPart;
    }

    private IntResult readInt(char firstDigit, boolean negative) {
        IntResult res = new IntResult(negative);
        if (firstDigit != NULL_CHAR) {
            res.push(firstDigit);
        }

        loop: while (pos < source.length) {
            char current = current();
            switch (current) {
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> res.push(current);
                case '-' -> {
                    if (!res.empty) break loop;
                    res.setNegative();
                }
                default -> { break loop; }
            }
            pos++; // consume character
        }
        return res;
    }

    private double readDecimalPart() {
        int oldPos = pos;
        double result = 0;
        long divider = 10;

        loop: while (pos < source.length) {
            char current = current();
            switch (current) {
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                    int value = current - '0';
                    result += (double) value / divider;
                    divider *= 10;
                    pos++; // consume character
                }
                default -> { break loop; }
            }
        }
        Assert.isTrue(pos > oldPos, "could not read the decimal part of a number");
        return result;
    }

    private class IntResult {
        private int value;
        private boolean negative; // if we have a negative sign before the number
        private boolean empty; // an absence of a number, meaning we haven't read any numbers

        public IntResult(boolean negative) {
            this.negative = negative;
            this.empty = true;
        }

        public void setNegative() {
            Assert.isFalse(negative, "cannot have two negative signs in a row");
            negative = true;
        }

        public void push(char digit) {
            value = value * 10 + (digit - '0');
            empty = false;
        }

        // support for -func() syntax
        public void alignState() {
            if (!empty) return;
            Assert.isTrue(Utility.isLowercaseLetter(peek()), "expected a number");
            // if there's a function after the - sign, change our value to -1 to do -1 * func()
            // negative is true, otherwise we wouldn't be here, so actual value is -1
            value = 1;
        }

        /*
        the tokenizer checks this flag because of the -0.xxx case
        remember this class is only used for reading a double, and we don't store the decimal part ourselves
        so the tokenizer might wrongly assume that we store a positive int value because -0 == 0
        so -0.123 will be read as 0.123 if we didn't keep track of this flag
         */
        public int getValue() {
            return negative ? -value : value;
        }
    }
}
