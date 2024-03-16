package me.fourteendoggo.mathexpressionparser.function;

import me.fourteendoggo.mathexpressionparser.symbol.Symbol;
import me.fourteendoggo.mathexpressionparser.symbol.SymbolType;
import me.fourteendoggo.mathexpressionparser.utils.Assert;

import java.util.function.ToDoubleFunction;

/**
 * A placeholder for an invokable function.
 * Invocation happens via {@link #apply(FunctionContext)}.
 * @see FunctionContext on how to deal with parameters.
 */
public class FunctionCallSite implements Symbol {
    private final String name;
    private final int minArgs, maxArgs;
    private final ToDoubleFunction<FunctionContext> function;

    public FunctionCallSite(String name, int numArgs, ToDoubleFunction<FunctionContext> function) {
        this(name, numArgs, numArgs, function);
    }

    public FunctionCallSite(String name, int minArgs, int maxArgs, ToDoubleFunction<FunctionContext> function) {
        Assert.isValidIdentifierName(name);
        Assert.isTrue(
                minArgs >= 0 && maxArgs >= 0 && maxArgs >= minArgs,
                "minArgs must be >= 0, maxArgs must be >= 0 and minArgs must be <= maxArgs"
        );
        this.name = name;
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
        this.function = function;
    }

    @Override
    public SymbolType getType() {
        return SymbolType.FUNCTION;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean supportsArgs() {
        return maxArgs > 0;
    }

    public FunctionContext allocateParameters() {
        return new FunctionContext();
    }

    public double apply(FunctionContext context) {
        int providedArgs = context.size();
        Assert.isFalse(providedArgs < minArgs, "not enough arguments provided (expected %s, got %s)", minArgs, providedArgs);
        Assert.isFalse(providedArgs > maxArgs, "too many arguments provided (max %s, got %s)", maxArgs, providedArgs);

        return function.applyAsDouble(context);
    }

    @Override
    public String toString() {
        return "FunctionCallSite{name='" + name + "', minArgs=" + minArgs + ", maxArgs=" + maxArgs + '}';
    }
}
