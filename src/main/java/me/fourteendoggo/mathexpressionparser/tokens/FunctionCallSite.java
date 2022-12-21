package me.fourteendoggo.mathexpressionparser.tokens;

import me.fourteendoggo.mathexpressionparser.Assert;

import java.util.function.ToDoubleFunction;

public class FunctionCallSite {
    private final int minArgs;
    private final ToDoubleFunction<double[]> solver;

    public FunctionCallSite(int minArgs, ToDoubleFunction<double[]> solver) {
        Assert.isFalse(minArgs < 0, "minArgs cannot be negative");
        this.minArgs = minArgs;
        this.solver = solver;
    }

    public double apply(double... args) {
        Assert.isTrue(args.length >= minArgs, "not enough arguments");
        return solver.applyAsDouble(args);
    }
}
