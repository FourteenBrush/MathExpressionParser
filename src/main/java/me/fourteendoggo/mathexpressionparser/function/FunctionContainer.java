package me.fourteendoggo.mathexpressionparser.function;

import me.fourteendoggo.mathexpressionparser.container.CharTree;
import me.fourteendoggo.mathexpressionparser.exceptions.FunctionNotFoundException;
import me.fourteendoggo.mathexpressionparser.utils.Utility;

import java.util.SplittableRandom;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

/**
 * A container to hold functions, global for each expression
 */
public class FunctionContainer {
    private static final SplittableRandom RANDOM = new SplittableRandom();
    private final CharTree<FunctionCallSite> functions = new CharTree<>(Utility::isLowercaseLetter);

    public static FunctionContainer empty() {
        return new FunctionContainer();
    }

    public static FunctionContainer withDefaultFunctions() {
        FunctionContainer container = new FunctionContainer();
        container.insertDefaultFunctions();
        return container;
    }

    private void insertDefaultFunctions() {
        insertFunction("sin", Math::sin);
        insertFunction("cos", Math::cos);
        insertFunction("tan", Math::tan);
        insertFunction("asin", Math::asin);
        insertFunction("acos", Math::acos);
        insertFunction("atan", Math::atan);
        insertFunction("sinh", Math::sinh);
        insertFunction("cosh", Math::cosh);
        insertFunction("tanh", Math::tanh);
        insertFunction("sqrt", Math::sqrt);
        insertFunction("cbrt", Math::cbrt);
        insertFunction("log", Math::log);
        insertFunction("rad", Math::toRadians);
        insertFunction("floor", Math::floor);
        insertFunction("ceil", Math::ceil);
        insertFunction("abs", d -> d < 0 ? -d : d);
        insertFunction("int", d -> (int) d);

        // constants
        insertFunction(new FunctionCallSite("pi", 0, ctx -> Math.PI));
        insertFunction(new FunctionCallSite("e", 0, ctx -> Math.E));

        // theoretical limit of Integer.MAX_VALUE
        insertFunction(new FunctionCallSite("min", 2, Integer.MAX_VALUE, ctx -> {
            double min = ctx.getDouble(0);
            for (int i = 1; i < ctx.size(); i++) {
                min = Math.min(min, ctx.getDouble(i));
            }
            return min;
        }));
        insertFunction(new FunctionCallSite("max", 2, Integer.MAX_VALUE, ctx -> {
            double max = ctx.getDouble(0);
            for (int i = 1; i < ctx.size(); i++) {
                max = Math.max(max, ctx.getDouble(i));
            }
            return max;
        }));
        insertFunction(new FunctionCallSite("avg", 2, Integer.MAX_VALUE, ctx -> {
            double sum = 0;
            for (int i = 0; i < ctx.size(); i++) {
                sum += ctx.getDouble(i);
            }
            return sum / ctx.size();
        }));
        insertFunction(new FunctionCallSite("round", 1, 2, ctx -> {
            if (ctx.size() == 1) {
                return Math.round(ctx.getDouble(0));
            }
            double value = ctx.getDouble(0);
            int places = ctx.getBoundedInt(1, 0, Integer.MAX_VALUE);
            double factor = Utility.getPowerOfTen(places);

            return Math.round(value * factor) / factor;
        }));
        insertFunction(new FunctionCallSite("rand", 0, 2, ctx -> switch (ctx.size()) {
            case 0 -> RANDOM.nextDouble();
            case 1 -> RANDOM.nextDouble(ctx.getDouble(0));
            default -> RANDOM.nextDouble(ctx.getDouble(0), ctx.getDouble(1)); // 2 args
        }));
        insertFunction(new FunctionCallSite("exit", 0, ctx -> {
            System.out.println("Exiting...");
            System.exit(0);
            throw new AssertionError("cannot return from system exit");
        }));
        insertFunction("and", (a, b) -> a != 0 && b != 0 ? 1 : 0);
        insertFunction("nand", (a, b) -> a != 0 && b != 0 ? 0 : 1);
        insertFunction("or", (a, b) -> a != 0 || b != 0 ? 1 : 0);
        insertFunction("xor", (a, b) -> a != 0 ^ b != 0 ? 1 : 0);
        insertFunction("not", a -> a == 0 ? 1 : 0);
        insertFunction("nor", (a, b) -> a != 0 || b != 0 ? 0 : 1);
        insertFunction("xnor", (a, b) -> a != 0 ^ b != 0 ? 0 : 1);
    }

    public void insertFunction(String functionName, DoubleUnaryOperator function) {
        insertFunction(new FunctionCallSite(functionName, 1, ctx -> {
            double first = ctx.getDouble(0);
            return function.applyAsDouble(first);
        }));
    }

    public void insertFunction(String functionName, DoubleBinaryOperator function) {
        insertFunction(new FunctionCallSite(functionName, 2, ctx -> {
            double first = ctx.getDouble(0);
            double second = ctx.getDouble(1);
            return function.applyAsDouble(first, second);
        }));
    }

    // TODO: whenever someone tries to insert a function with a partially invalid name
    // and decides to catch and ignore the exception being thrown, the correct part of the function name will be inserted
    // all functions still work but we might want to avoid this
    public void insertFunction(FunctionCallSite function) {
        char[] name = function.getName().toCharArray();
        functions.insert(name, function);
    }

    public FunctionCallSite getFunction(char[] buffer, int fromPos) {
        FunctionCallSite function = functions.search(buffer, fromPos);
        if (function == null) {
            // some unoptimized stuff, we are throwing an exception anyway, so it's not that bad
            String bufferAsString = new String(buffer, fromPos, buffer.length - fromPos);
            // there might be a better way to get the entered function name but as long as it works its good
            String functionName = bufferAsString.split("[^a-zA-Z]")[0];
            throw new FunctionNotFoundException("function " + functionName + " not found");
        }
        return function;
    }
}
