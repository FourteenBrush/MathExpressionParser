package me.fourteendoggo.mathexpressionparser.function;

/**
 * A class that provides parameters for a {@link FunctionCallSite}. <br/>
 * Reserves space for 10 parameters by default, but is dynamically resizable.
 */
public class FunctionContext {
    private double[] parameters = new double[10];
    private int size;

    /**
     * Adds a parameter to this context, increasing the internal array if necessary.
     * This should not be called by the user.
     * @param value the parameter to add
     */
    public void add(double value) {
        if (size == parameters.length) {
            double[] newParameters = new double[size * 2];
            System.arraycopy(parameters, 0, newParameters, 0, parameters.length);
            parameters = newParameters;
        }
        parameters[size++] = value;
    }

    /**
     * Returns a parameter at the specified index.
     * @param index the index, must be between 0 and {@link #size()} - 1
     * @return the parameter at that specified index
     */
    public double get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index " + index + " is out of bounds for size " + size
                    + ", do not call FunctionContext#get with an index that is not in the range [0, " + (size - 1) + "]");
        }
        return parameters[index];
    }

    /**
     * @return the number of parameters in this context
     */
    public int size() {
        return size;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            sb.append(parameters[i]);
            if (i != size - 1) sb.append(", ");
        }
        return sb.append(']').toString();
    }
}
