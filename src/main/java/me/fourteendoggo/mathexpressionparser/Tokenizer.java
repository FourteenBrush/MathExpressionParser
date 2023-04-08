package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.function.FunctionCallSite;
import me.fourteendoggo.mathexpressionparser.function.FunctionContainer;
import me.fourteendoggo.mathexpressionparser.function.FunctionContext;
import me.fourteendoggo.mathexpressionparser.tokens.Operand;
import me.fourteendoggo.mathexpressionparser.utils.Assert;
import me.fourteendoggo.mathexpressionparser.utils.CharConsumer;
import me.fourteendoggo.mathexpressionparser.utils.Utility;

import java.util.function.IntPredicate;

public class Tokenizer {
    static final FunctionContainer FUNCTION_CONTAINER = FunctionContainer.withDefaultFunctions();
    static final char NULL_CHAR = '\0';
    private final char[] input;
    private final IntPredicate loopCondition;
    private int pos;

    public Tokenizer(char[] input, IntPredicate loopCondition) {
        this.input = input;
        this.loopCondition = loopCondition;
    }

    public void advance() {
        pos++;
    }

    public char current() {
        return input[pos];
    }

    private char peek() {
        if (pos >= input.length) return NULL_CHAR;
        return input[pos];
    }

    public void forEachRemaining(CharConsumer consumer) {
        // can't use peek() here because the consumer will accept NULL_CHAR
        while (pos < input.length && loopCondition.test(input[pos])) {
            consumer.accept(input[pos]);
        }
    }

    public boolean hasAndGet(char expected) {
        if (peek() != expected) {
            return false;
        }
        advance();
        return true;
    }

    private void expectAndGet(char expected, String exceptionMessage) {
        Assert.isTrue(hasAndGet(expected), exceptionMessage);
    }

    public Operand readBrackets() {
        Expression sub = new Expression(input, Utility::isBetweenBrackets);
        Tokenizer subTokenizer = sub.getTokenizer();

        subTokenizer.pos = pos + 1; // enter expression, we are sure we read an opening parenthesis
        Operand result = new Operand(sub.parse());
        pos = subTokenizer.pos; // resume reading behind the closing parenthesis
        Assert.isTrue(peek() == ')', "missing closing parenthesis");

        return result;
    }

    public Operand readFunction() {
        FunctionCallSite function = FUNCTION_CONTAINER.getFunction(input, pos);
        pos += function.getName().length(); // skip function name
        expectAndGet('(', "missing opening parenthesis");

        FunctionContext parameters = function.allocateParameters();
        // only read parameters if the function supports it or if there are optional parameters filled in
        // if current points to anything other than a closing parenthesis, we know we got a parameter
        if (peek() != ')') {
            Assert.isTrue(function.supportsArgs(), "did not expect and parameters for function %s", function.getName());
            Expression parameter = new Expression(input, Utility::isValidArgument);
            parameter.getTokenizer().pos = pos; // position them to read the first parameter
            readParameterList(parameters, parameter);
        }
        expectAndGet(')', "missing closing parenthesis");

        return new Operand(function.apply(parameters));
    }

    private void readParameterList(FunctionContext parameters, Expression parameter) {
        parameters.add(parameter.parse());
        Tokenizer tokenizer = parameter.getTokenizer();

        while (tokenizer.peek() == ',') {
            int oldPos = tokenizer.pos;

            parameter = new Expression(input, Utility::isValidArgument);
            tokenizer = parameter.getTokenizer();

            tokenizer.pos = oldPos + 1;
            parameters.add(parameter.parse());
        }
        pos = tokenizer.pos;
    }

    public Operand readOperand(char firstDigit, boolean negative) {
        double value = readDouble(firstDigit, negative);
        return new Operand(value);
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

        loop: while (pos < input.length) {
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

        loop: while (pos < input.length) {
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

    @Override
    public String toString() {
        return new String(input, pos, input.length - pos);
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
