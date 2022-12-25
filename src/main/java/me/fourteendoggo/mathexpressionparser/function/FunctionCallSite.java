package me.fourteendoggo.mathexpressionparser.function;

import me.fourteendoggo.mathexpressionparser.Assert;

import java.util.function.ToDoubleFunction;

/**
 * A placeholder for an invokable function.
 */
public class FunctionCallSite {
    private final String name;
    private final int minArgs, maxArgs;
    private final ToDoubleFunction<FunctionContext> function;

    public FunctionCallSite(String name, int numArgs, ToDoubleFunction<FunctionContext> function) {
        this(name, numArgs, numArgs, function);
    }

    public FunctionCallSite(String name, int minArgs, int maxArgs, ToDoubleFunction<FunctionContext> function) {
        if (minArgs < 0 || maxArgs < 0 || minArgs > maxArgs) {
            throw new IllegalArgumentException("minArgs must be >= 0, maxArgs must be >= 0 and minArgs must be <= maxArgs");
        }
        this.name = name.trim();
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
        this.function = function;
    }

    public String getName() {
        return name;
    }

    public int getMinArgs() {
        return minArgs;
    }

    public int getLength() {
        return name.length();
    }

    public double apply(FunctionContext context) {
        int argsLength = context.size();
        Assert.isTrue(argsLength >= minArgs, "not enough arguments provided (" + argsLength + " < " + minArgs + ")");
        Assert.isTrue(argsLength <= maxArgs, "too many arguments provided (" + argsLength + " > " + maxArgs + ")");

        return function.applyAsDouble(context);
    }

    @Override
    public String toString() {
        return "FunctionCallSite{name='" + name + "', minArgs=" + minArgs +
                ", maxArgs=" + maxArgs + '}';
    }
}
