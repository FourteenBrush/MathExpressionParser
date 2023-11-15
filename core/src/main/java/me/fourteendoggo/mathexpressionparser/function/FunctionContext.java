package me.fourteendoggo.mathexpressionparser.function;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.utils.Assert;
import me.fourteendoggo.mathexpressionparser.utils.Utility;

import java.util.Arrays;

/**
 * A class that provides parameters for a {@link FunctionCallSite}. Acts as an array of doubles.
 */
public class FunctionContext {
    private double[] parameters;
    private int size;

    /**
     * Creates a new FunctionContext with a given initial capacity for holding parameters
     * @param initialCapacity the initial capacity
     */
    FunctionContext(int initialCapacity) { // package-private
        parameters = new double[initialCapacity];
    }

    /**
     * @return the number of parameters in this context
     */
    public int size() {
        return size;
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
     * Either returns an unsigned int or fails.
     * @see #getBoundedInt(int, int, int)
     */
    public int getUnsignedInt(int index, int max) {
        return getBoundedInt(index, 0, max);
    }

    /**
     * Converts a parameter to an integer, ensuring no precision loss occurs and that the value falls within the specified range.
     * @param index the index of the parameter
     * @param min the minimum value that the integer is expected to be
     * @param max the maximum value the integer is expected to be
     * @return the parameter at that position
     * @throws SyntaxException if any precision loss occurs while casting {@link #getDouble(int)} to an integer
     * or if the value doesn't fall within the specified range
     * @see #getInt(int)
     */
    public int getBoundedInt(int index, int min, int max) {
        int value = getInt(index);
        if (value < min || value > max) {
            throw new SyntaxException("expected an integer between %s and %s as %s argument, got %s",
                    min, max, Utility.getOrdinalName(index), value);
        }
        return value;
    }

    /**
     * Converts the result of {@link #getDouble(int)} to an integer
     * @throws SyntaxException if any precision loss occurs while casting {@link #getDouble(int)} to an integer
     * @see #getDouble(int)
     */
    public int getInt(int index) {
        double value = getDouble(index);
        int intValue = (int) value;
        if (intValue != value) {
            throw new SyntaxException("expected an integer as %s arguments, got %s", Utility.getOrdinalName(index), value);
        }
        return intValue;
    }

    /**
     * Returns a parameter, ensuring that it falls within the specified range.
     * @param index the index of the parameter
     * @param min the minimum value that the parameter is expected to be
     * @param max the maximum value the parameter is expected to be
     * @return the parameter at that position
     * @throws SyntaxException if the value doesn't fall within the specified range
     */
    public double getBoundedDouble(int index, double min, double max) {
        double value = getDouble(index);
        if (value < min || value > max) {
            throw new SyntaxException("expected a value between %s and %s as %s argument, got %s", Utility.getOrdinalName(index), value);
        }
        return value;
    }

    /**
     * @param index the index, between 0 and {@link #size()} - 1
     * @return the parameter at the given index
     */
    public double getDouble(int index) {
        Assert.indexWithinBounds(
                index, size, "index %s is out of bounds for size %s, function definition is probably set up wrongly",
                index, size, size - 1
        );
        return parameters[index];
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
