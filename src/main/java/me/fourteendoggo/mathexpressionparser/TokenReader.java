package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;

import java.nio.CharBuffer;

public class TokenReader {
    private final CharBuffer buffer;

    public TokenReader(char[] input) {
        this.buffer = CharBuffer.wrap(input);
    }

    public char get() {
        return buffer.get();
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

    public void revert() {
        position(position() - 1);
    }

    /**
     * Attempts to read an integer from the internal buffer
     * If we are at the end or the next character is not a digit, we return 0
     * This positions our {@link #position()} right after the end of the integer
     *
     * @return the integer read from the buffer or 0 if no integer was found
     */
    public int readInt() {
        IntResult result = readInt0();
        return result.value;
    }

    private IntResult readInt0() {
        boolean negative = false;
        int result = 0;
        int decimalDivider = 1;

        loop: while (buffer.hasRemaining()) {
            char current = get();

            switch (current) {
                case '-' -> {
                    if (negative) {
                        // decimalDivider being 1 would mean that the number is empty, meaning --x
                        Validate.isFalse(decimalDivider == 1, "cannot have two negative signs in a row");
                        revert();
                        break loop;
                    }
                    if (decimalDivider > 1) {
                        // we read a number, then encountered a negative sign, go back one
                        revert();
                        break loop;
                    }
                    negative = true;
                    continue; // do not increment decimalDivider
                }
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                    result *= 10;
                    result += current - '0';
                }
                default -> {
                    // we read a character that is not part of the number, put the last character back and break
                    revert();
                    break loop;
                }
            }
            decimalDivider *= 10;
        }
        return new IntResult(negative ? -result : result, decimalDivider);
    }

    /**
     * A method to read doubles from the internal buffer, starting at {@link #position()}
     * Uses {@link #readInt()} internally to read the integer parts of the number
     *
     * @see #readInt()
     * @return the double read from the buffer or 0.0 if no double was found
     */
    public double readDouble() {
        int beforeComma = readInt();
        if (!hasRemaining() || current() != '.') {
            return beforeComma;
        }

        get(); // skip the decimal point
        IntResult behindComma = readInt0();
        if (behindComma.isBlank()) { // we only found a decimal point, no numbers after it
            throw new SyntaxException("read a decimal point but no value comes behind it");
        }
        double decimalPart = behindComma.value / (double) behindComma.decimalDivider;
        return beforeComma + decimalPart;
    }

    @Override
    public String toString() {
        return "TokenReader{buffer=" + buffer + '}';
    }

    /**
     * A pair of two ints, used by internal methods to handle decimal values
     * <p>
     * This is useful because the {@link #readInt()} method only handles integers
     * and no doubles could've been read otherwise
     * @param value the actual value being read
     * @param decimalDivider basically Math.pow(10, length of the value)<p> used in methods that read decimal values behind the comma
     * <br/>
     *
     * <pre>
     * {@code
     *  NioCharBuffer buffer = new TokenReader("0.123".toCharArray());
     *  // move to index 2 to skip the "0." part
     *  buffer.position(2);
     *  IntResult result = buffer.readInt0();
     *  // value = 123
     *  // decimalDivider = 1000
     *  int actualResult = result.value / (double) result.decimalDivider;
     *  assert actualResult == 0.123;
     * }
     * </pre>
     */
    private record IntResult(int value, int decimalDivider) {
        // 1 as decimalDivider means that we did not read any numbers
        public boolean isBlank() {
            return value == 0 && decimalDivider == 1;
        }
    }
}
