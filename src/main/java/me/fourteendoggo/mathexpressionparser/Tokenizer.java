package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.container.ReadOnlyCharBuffer;
import me.fourteendoggo.mathexpressionparser.function.FunctionCallSite;
import me.fourteendoggo.mathexpressionparser.function.FunctionContainer;
import me.fourteendoggo.mathexpressionparser.function.FunctionContext;
import me.fourteendoggo.mathexpressionparser.tokens.Operand;

import java.util.function.IntPredicate;

public class Tokenizer {
    static final FunctionContainer FUNCTION_CONTAINER = FunctionContainer.createDefault(); // package-private
    private final ReadOnlyCharBuffer buffer;

    public Tokenizer(char[] input, IntPredicate loopCondition) {
        buffer = new ReadOnlyCharBuffer(input, loopCondition);
    }

    public void next() {
        buffer.get();
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
        return buffer.current();
    }

    public Operand readBrackets() {
        Expression sub = new Expression(buffer.array(), current -> current != ')');
        ReadOnlyCharBuffer subBuffer = sub.getTokenizer().buffer;

        subBuffer.position(position() + 1); // we are sure we read an opening bracket, see Expression
        Operand result = new Operand(sub.parse());
        subBuffer.expectUnchecked(')', "expected a closing bracket");
        position(subBuffer.position()); // resume reading behind the closing bracket

        return result;
    }

    public Operand readFunction() {
        FunctionCallSite function = FUNCTION_CONTAINER.search(buffer.array(), position());
        buffer.move(function.getLength()); // skip the function name
        buffer.expectAndGet('(', "expected opening parenthesis after function name");

        FunctionContext parameters = new FunctionContext();
        // Expression will disallow reading () so we don't try to read parameters if the minArgs == 0
        if (function.getMinArgs() > 0) {
            Expression sub = new Expression(buffer.array(), current -> current != ',' && current != ')');
            ReadOnlyCharBuffer subBuffer = sub.getTokenizer().buffer;
            subBuffer.position(position()); // let them start reading the parameter list

            readParameterList(parameters, sub, subBuffer);
        }
        buffer.expectAndGet(')', "expected closing parenthesis after function parameters");

        return new Operand(function.apply(parameters));
    }

    private void readParameterList(FunctionContext output, Expression sub, ReadOnlyCharBuffer subBuffer) {
        output.add(sub.parse());

        while (subBuffer.hasRemainingUnchecked() && subBuffer.current() == ',') {
            int oldPosition = subBuffer.position();

            sub = new Expression(subBuffer.array(), current -> current != ',' && current != ')');
            subBuffer = sub.getTokenizer().buffer;

            subBuffer.position(oldPosition + 1);
            output.add(sub.parse());
        }
        position(subBuffer.position());
    }

    public Operand readOperand(char firstNumber, boolean negative) {
        double value = readDouble(firstNumber, negative);
        return new Operand(value);
    }

    private double readDouble(char firstNumber, boolean negative) {
        IntResult beforeComma = readInt0(firstNumber, negative);
        if (!buffer.hasAndGet('.')) { // skips decimal point if one was present
            return beforeComma.value();
        }

        double decimalPart = readDecimalPart();

        if (beforeComma.negative()) {
            return beforeComma.value() - decimalPart;
        }
        return beforeComma.value() + decimalPart;
    }

    private IntResult readInt0(char firstNumber, boolean negative) {
        int result = firstNumber - '0';
        // TODO: this isn't used anymore but we still need a way to ensure something is read
        int decimalDivider = firstNumber >= '0' && firstNumber <= '9' ? 10 : 1;

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
                    next(); // consume the negative sign
                    continue; // do not change decimalDivider because this is just a negative sign and not a part of a number
                }
                default -> {
                    break loop;
                }
            }
            next(); // consume the character
            decimalDivider *= 10;
        }
        // fix -.1 being read as -0.1
        // TODO: decimal divider overflow issue when the number became very big/ small causes this to trigger sometimes
        Assert.isTrue(decimalDivider > 1, "no value comes behind the negative sign");
        // see IntResult for more info why we don't do negative ? -result : result
        return new IntResult(result, negative, decimalDivider);
    }

    private double readDecimalPart() {
        int oldPos = position();
        double result = 0;
        long divider = 10;

        loop: while (hasRemaining()) {
            char current = current();
            switch (current) {
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                    int value = current - '0';
                    result += (double) value / divider;
                    divider *= 10;
                    next(); // consume character
                }
                default -> { break loop; }
            }
        }
        Assert.isTrue(position() > oldPos, "could not read the decimal part of a number");
        return result;
    }

    @Override
    public String toString() {
        return "Tokenizer{buffer=" + buffer + '}';
    }

    /**
     * The result of an attempted try to read an integer from the buffer
     * This is used by internal methods to handle the fractional part of a double
     *
     * @param value the value of the integer read
     * @param negative a redundant way to check whether the value is negative, because the value might be 0
     * @param decimalDivider the result of {@code Math.pow(10, String.valueOf(value).length())} <br/>
     *                       To decide how big the fractional part of a number should be <br/>
     *                       (e.g. 0.25 would be value: 25 / decimalDivider: 100)
     */
    private record IntResult(int value, boolean negative, int decimalDivider) {
        @Override
        public int value() {
            return negative ? -value : value;
        }
    }
}
