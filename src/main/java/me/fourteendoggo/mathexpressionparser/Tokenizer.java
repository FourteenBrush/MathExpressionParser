package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.function.FunctionCallSite;
import me.fourteendoggo.mathexpressionparser.function.FunctionContainer;
import me.fourteendoggo.mathexpressionparser.function.FunctionContext;
import me.fourteendoggo.mathexpressionparser.tokens.Operand;

import java.nio.CharBuffer;

public class Tokenizer {
    static final FunctionContainer FUNCTION_CONTAINER = new FunctionContainer(); // package-private
    private final CharBuffer buffer;

    public Tokenizer(char[] input) {
        buffer = CharBuffer.wrap(input);
    }

    public void next() {
        buffer.get();
    }

    public void move(int steps) {
        position(position() + steps);
    }

    public int position() {
        return buffer.position();
    }

    public boolean hasRemaining() {
        return buffer.hasRemaining();
    }

    public void position(int pos) {
        buffer.position(pos);
    }

    public char current() {
        return buffer.get(position());
    }

    public Operand readFunction() {
        FunctionCallSite function = FUNCTION_CONTAINER.search(buffer.array(), position());
        move(function.getLength());
        Assert.isTrue(hasRemaining() && buffer.get() == '(', "expected '(' after function name");

        FunctionContext parameters = new FunctionContext();
        // Expression will disallow reading () so we just don't try to read parameters if the minArgs == 0
        if (function.getMinArgs() > 0) {
            Expression sub = new Expression(buffer.array(), current -> current != ',' && current != ')');
            Tokenizer tokenizer = sub.getTokenizer();
            tokenizer.position(position()); // modify their position to start reading behind the '('

            readParameterList(parameters, sub, tokenizer);
        }
        Assert.isTrue(hasRemaining() && buffer.get() == ')', "expected ')' after function parameter list");

        return new Operand(function.apply(parameters));
    }

    private void readParameterList(FunctionContext source, Expression sub, Tokenizer tokenizer) {
        source.add(sub.parse());

        while (tokenizer.hasRemaining() && tokenizer.current() == ',') {
            int oldPos = tokenizer.position();
            // parse individual parameter
            sub = new Expression(buffer.array(), current -> current != ',' && current != ')');
            tokenizer = sub.getTokenizer();
            tokenizer.position(oldPos + 1); // move behind the ','
            source.add(sub.parse());
        }
        position(tokenizer.position()); // update our position to the ')'
    }

    public Operand readOperand() {
        return readOperand('0');
    }

    public Operand readOperand(char firstChar) {
        double value = readDouble(firstChar);
        return new Operand(value);
    }

    /**
     * @see Tokenizer#readDouble(char)
     */
    public double readDouble() {
        return readDouble('0');
    }

    /**
     * Attempts to read a double from the internal buffer, starting at {@link #position()}
     * and therefore moving the position forward to the end position of the double + 1. <br/>
     * If we reached the end of the buffer, or it doesn't contain a number, 0.0 is returned.
     * @param firstChar the first character that is already been read (see {@link Expression#parse()})
     * @return the double read from the buffer or 0.0 if no double was found
     */
    public double readDouble(char firstChar) {
        IntResult beforeComma = readInt0(firstChar);
        int beforeCommaValue = beforeComma.value();
        if (!hasRemaining() || current() != '.') {
            return beforeCommaValue;
        }

        next(); // skip the decimal point
        IntResult behindComma = readInt0();
        Assert.isFalse(behindComma.isBlank(), "read a decimal point but no value comes behind it");
        // always unsigned
        double decimalPart = behindComma.value() / (double) behindComma.decimalDivider();
        // fix -0.25 being read as 0.25 and -1.25 being read as -0.75
        // when the beforeCommaValue is 0, we cant check < 0 because -0 doesn't exist
        if (beforeCommaValue < 0 || beforeComma.negative()) {
            decimalPart *= -1;
        }
        return beforeCommaValue + decimalPart;
    }

    // TODO: use this for optimisation purposes
    // might actually use a a custom functions with readDouble() instead
    /**
     * Attempts to read an integer from the internal buffer
     * If we couldn't read anything or there is no number, we return a 0
     * This positions our {@link #position()} right after the end of the integer
     *
     * @return the integer read from the buffer or 0 if no integer could be read
     */
    public int readInt() {
        // let's not copy that readInt0() method twice
        IntResult result = readInt0();
        return result.value();
    }

    private IntResult readInt0() {
        return readInt0('0');
    }

    private IntResult readInt0(char firstChar) {
        boolean negative = false;
        int result = firstChar - '0';
        int decimalDivider = result > 0 ? 10 : 1;

        loop: while (hasRemaining()) {
            char current = current();
            switch (current) {
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> result = result * 10 + (current - '0');
                case '-' -> {
                    // we already have a negative sign, and no number (decimalDivider being its initial value)
                    Assert.isFalse(negative && decimalDivider == 1, "cannot have two negative signs in a row");

                    if (decimalDivider > 1) {
                        // encountered a subtraction operator (-x - ...)
                        break loop;
                    }
                    negative = true;
                    next(); // consume the '-' character
                    continue; // do not change decimalDivider because this is just a negative sign and not a part of a number
                }
                default -> {
                    break loop;
                }
            }
            next(); // consume the character
            decimalDivider *= 10;
        }
        // see IntResult for more info why we don't do negative ? -result : result
        return new IntResult(result, negative, decimalDivider);
    }

    @Override
    public String toString() {
        return "Tokenizer{buffer=" + buffer + '}';
    }

    /**
     * The result of an attempted try to read an integer from the buffer
     * This is used by internals methods to handle the fractional part of a double
     *
     * @param value the value of the integer read
     * @param decimalDivider the result of {@code Math.pow(10, String.valueOf(value).length())} <br/>
     *                       To decide how big the fractional part of a number should be <br/>
     *                       (e.g. 0.25 would be value: 25 / decimalDivider: 100)
     */
    private record IntResult(int value, boolean negative, int decimalDivider) {
        @Override
        public int value() {
            return negative ? -value : value;
        }

        public boolean isBlank() {
            // 1 as decimalDivider (initial position) means that we did not read any numbers
            return value == 0 && decimalDivider == 1;
        }
    }
}
