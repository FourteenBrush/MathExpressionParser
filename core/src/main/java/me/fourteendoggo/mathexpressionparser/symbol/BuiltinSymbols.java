package me.fourteendoggo.mathexpressionparser.symbol;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.function.FunctionCallSite;
import me.fourteendoggo.mathexpressionparser.utils.Utility;

import java.util.SplittableRandom;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

public abstract class BuiltinSymbols {
    private static final SplittableRandom RANDOM = new SplittableRandom();

    public static void init(SymbolLookup lookup) {
        // trigonometric
        insertFunction(lookup, "sin", Math::sin);
        insertFunction(lookup, "cos", Math::cos);
        insertFunction(lookup, "tan", Math::tan);
        insertFunction(lookup, "asin", Math::asin);
        insertFunction(lookup, "acos", Math::acos);
        insertFunction(lookup, "atan", Math::atan);
        insertFunction(lookup, "sinh", Math::sinh);
        insertFunction(lookup, "cosh", Math::cosh);
        insertFunction(lookup, "tanh", Math::tanh);
        insertFunction(lookup, "sqrt", Math::sqrt);
        insertFunction(lookup, "cbrt", Math::cbrt);

        insertFunction(lookup, "pow", Math::pow);
        insertFunction(lookup, "log", Math::log);
        insertFunction(lookup, "rad", Math::toRadians);
        insertFunction(lookup, "floor", Math::floor);
        insertFunction(lookup, "ceil", Math::ceil);
        insertFunction(lookup, "abs", Math::abs);
        insertFunction(lookup, "int", d -> (int) d);
        // boolean
        insertFunction(lookup, "and", Utility::boolAnd);
        insertFunction(lookup, "nand", (a, b) -> a != 0 && b != 0 ? 0 : 1);
        insertFunction(lookup, "or", Utility::boolOr);
        insertFunction(lookup, "xor", (a, b) -> a != 0 ^ b != 0 ? 1 : 0);
        insertFunction(lookup, "not", Utility::boolNot);
        insertFunction(lookup, "nor", (a, b) -> a != 0 || b != 0 ? 0 : 1);
        insertFunction(lookup, "xnor", (a, b) -> a != 0 ^ b != 0 ? 0 : 1);
        // no idea what this one is useful for, doubles are already representable as booleans
        // maybe to transform a double to either 1 or 0
        insertFunction(lookup, "bool", d -> d == 0 ? 0 : 1);
        // time-related
        lookup.insert(new FunctionCallSite("now", 0, ctx -> (double) System.currentTimeMillis())); // a long, hmm ye...
        // constants
        lookup.insert(new Variable("pi", Math.PI));
        lookup.insert(new Variable("e", Math.E));
        lookup.insert(new Variable("true", 1));
        lookup.insert(new Variable("false", 0));

        // theoretical limit of Integer.MAX_VALUE parameters
        lookup.insert(new FunctionCallSite("min", 2, Integer.MAX_VALUE, ctx -> {
            double min = ctx.getDouble(0);
            for (int i = 1; i < ctx.size(); i++) {
                min = Math.min(min, ctx.getDouble(i));
            }
            return min;
        }));
        lookup.insert(new FunctionCallSite("max", 2, Integer.MAX_VALUE, ctx -> {
            double max = ctx.getDouble(0);
            for (int i = 1; i < ctx.size(); i++) {
                max = Math.max(max, ctx.getDouble(i));
            }
            return max;
        }));
        lookup.insert(new FunctionCallSite("avg", 2, Integer.MAX_VALUE, ctx -> {
            double sum = 0;
            for (int i = 0; i < ctx.size(); i++) {
                sum += ctx.getDouble(i);
            }
            return sum / ctx.size();
        }));
        lookup.insert(new FunctionCallSite("sum", 2, Integer.MAX_VALUE, ctx -> {
            double sum = ctx.getDouble(0);
            for (int i = 1; i < ctx.size(); i++) {
                sum += ctx.getDouble(i);
            }
            return sum;
        }));
        lookup.insert(new FunctionCallSite("round", 1, 2, ctx -> {
            if (ctx.size() == 1) {
                return Math.round(ctx.getDouble(0));
            }
            double value = ctx.getDouble(0);
            int places = ctx.getBoundedInt(1, 0, Integer.MAX_VALUE);
            double factor = Utility.getPowerOfTen(places);

            return Math.round(value * factor) / factor;
        }));
        lookup.insert(new FunctionCallSite("rand", 0, 2, ctx -> {
            try {
                return switch (ctx.size()) {
                    case 0 -> RANDOM.nextDouble();
                    case 1 -> RANDOM.nextDouble(ctx.getDouble(0));
                    default -> RANDOM.nextDouble(ctx.getDouble(0), ctx.getDouble(1));
                };
            } catch (IllegalArgumentException e) {
                // random throws this when f.e. the bound > the origin, translate this into a SyntaxException
                throw new SyntaxException(e.getMessage());
            }
        }));
        // must be handled with care as people can and will abuse this, maybe add a permission or overwrite this function?
        lookup.insert(new FunctionCallSite("exit", 0, ctx -> {
            System.out.println("Exiting...");
            System.exit(0);
            throw new AssertionError("cannot return from system exit");
        }));
    }

    private static void insertFunction(SymbolLookup lookup, String name, DoubleUnaryOperator fn) {
        lookup.insert(new FunctionCallSite(name, 1, ctx -> {
            double first = ctx.getDouble(0);
            return fn.applyAsDouble(first);
        }));
    }

    private static void insertFunction(SymbolLookup lookup, String name, DoubleBinaryOperator fn) {
        lookup.insert(new FunctionCallSite(name, 2, ctx -> {
            double first = ctx.getDouble(0);
            double second = ctx.getDouble(1);
            return fn.applyAsDouble(first, second);
        }));
    }
}
