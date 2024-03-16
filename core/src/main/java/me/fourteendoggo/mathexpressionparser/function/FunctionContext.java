package me.fourteendoggo.mathexpressionparser.function;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.utils.Assert;
import me.fourteendoggo.mathexpressionparser.utils.Utility;

import java.util.Arrays;

// FIXME: implement PrimitiveIterator.OfDouble maybe?
/**
 * A class that provides parameters for a {@link FunctionCallSite}. Acts as an array of doubles.
 */
public class FunctionContext {
    private double[] parameters;
    private int size;

    /**
     * @see #FunctionContext(int)
     */
    FunctionContext() {
        this(16);
    }

    /**
     * Creates a new FunctionContext with a given initial capacity for holding parameters
     * @param initialCapacity the initial capacity
     */
    FunctionContext(int initialCapacity) {
        parameters = new double[initialCapacity];
    }

    /**
     * @return the number of parameters in this context
     */
    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Adds a parameter to this context, resizing the internal array if necessary.
     * This should not be called to modify any parameters after they have been set.
     * @param value the parameter to add
     */
    public void add(double value) {
        if (size == parameters.length) {
            parameters = Arrays.copyOf(parameters, size * 2 + 1);
        }
        parameters[size++] = value;
    }

    /**
     * @see #getUnsignedInt(int, int)
     */
    public int getUnsignedInt(int idx) {
        return getUnsignedInt(idx, Integer.MAX_VALUE);
    }

    /**
     * Either returns an unsigned int or fails.
     * @see #getBoundedInt(int, int, int)
     */
    public int getUnsignedInt(int idx, int max) {
        return getBoundedInt(idx, 0, max);
    }

    /**
     * Converts a parameter to an integer, ensuring no precision loss occurs and that the value falls within the specified range.
     * @param idx the index of the parameter
     * @param min the minimum value that the integer is expected to be
     * @param max the maximum value the integer is expected to be
     * @return the parameter at that position
     * @throws SyntaxException if any precision loss occurs while casting {@link #getDouble(int)} to an integer
     * or if the value doesn't fall within the specified range
     * @see #getInt(int)
     */
    public int getBoundedInt(int idx, int min, int max) {
        int value = getInt(idx);
        if (value < min || value > max) {
            throw new SyntaxException("expected an integer between %s and %s as %s argument, got %s",
                    min, max, Utility.getOrdinalName(idx), value);
        }
        return value;
    }

    /**
     * Converts the result of {@link #getDouble(int)} to an integer
     * @throws SyntaxException if any precision loss occurs while casting {@link #getDouble(int)} to an integer
     * @see #getDouble(int)
     */
    public int getInt(int idx) {
        double value = getDouble(idx);
        int intValue = (int) value;
        if (intValue != value) {
            throw new SyntaxException("expected an integer as %s argument, got %s", Utility.getOrdinalName(idx), value);
        }
        return intValue;
    }

    /**
     * @see #getUnsignedDouble(int, double)
     */
    public double getUnsignedDouble(int idx) {
        return getBoundedDouble(idx, 0, Double.MAX_VALUE);
    }

    /**
     * Either returns an unsigned double or fails.
     * @see #getBoundedDouble(int, double, double)
     */
    public double getUnsignedDouble(int idx, double max) {
        return getBoundedDouble(idx, 0, max);
    }

    /**
     * Returns a parameter, ensuring that it falls within the specified range.
     * @param idx the index of the parameter
     * @param min the minimum value that the parameter is expected to be
     * @param max the maximum value the parameter is expected to be
     * @return the parameter at that position
     * @throws SyntaxException if the value doesn't fall within the specified range
     */
    public double getBoundedDouble(int idx, double min, double max) {
        double value = getDouble(idx);
        if (value < min || value > max) {
            throw new SyntaxException("expected a value between %s and %s as %s argument, got %s", Utility.getOrdinalName(idx), value);
        }
        return value;
    }

    /**
     * @param idx the index, between 0 and {@link #size()} - 1
     * @return the parameter at the given index
     */
    public double getDouble(int idx) {
        Assert.indexWithinBounds(
                idx, size, "index %s is out of bounds for size %s, function definition is probably set up wrongly",
                idx, size
        );
        return parameters[idx];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            sb.append(parameters[i]);
            if (i != size - 1) {
                sb.append(", ");
            }
        }
        return sb.append(']').toString();
    }
}
