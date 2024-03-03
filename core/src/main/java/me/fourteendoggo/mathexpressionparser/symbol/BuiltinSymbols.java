package me.fourteendoggo.mathexpressionparser.symbol;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.utils.Assert;
import me.fourteendoggo.mathexpressionparser.utils.Utility;

import java.util.SplittableRandom;

/**
 * A utility class used to create an {@link ExecutionEnv}, filled with all default functions.
 */
abstract class BuiltinSymbols {

    /**
     * @return an {@link ExecutionEnv}, filled with all default functions.
     */
    static ExecutionEnv createExecutionEnv() {
        ExecutionEnv env = new ExecutionEnv();

        // trigonometric
        env.insertFunction("sin", Math::sin);
        env.insertFunction("cos", Math::cos);
        env.insertFunction("tan", Math::tan);
        env.insertFunction("asin", Math::asin);
        env.insertFunction("acos", Math::acos);
        env.insertFunction("atan", Math::atan);
        env.insertFunction("sinh", Math::sinh);
        env.insertFunction("cosh", Math::cosh);
        env.insertFunction("tanh", Math::tanh);
        env.insertFunction("sqrt", Math::sqrt);
        env.insertFunction("cbrt", Math::cbrt);

        env.insertFunction("pow", Math::pow);
        env.insertFunction("log", Math::log);
        env.insertFunction("rad", Math::toRadians);
        env.insertFunction("floor", Math::floor);
        env.insertFunction("ceil", Math::ceil);
        env.insertFunction("abs", Math::abs);
        env.insertFunction("int", d -> (int) d);
        // boolean
        env.insertFunction("and", Utility::boolAnd);
        env.insertFunction("nand", (a, b) -> a != 0 && b != 0 ? 0 : 1);
        env.insertFunction("or", Utility::boolOr);
        env.insertFunction("xor", (a, b) -> a != 0 ^ b != 0 ? 1 : 0);
        env.insertFunction("not", Utility::boolNot);
        env.insertFunction("nor", (a, b) -> a != 0 || b != 0 ? 0 : 1);
        env.insertFunction("xnor", (a, b) -> a != 0 ^ b != 0 ? 0 : 1);
        // no idea what this one is useful for, doubles are already representable as booleans
        // maybe to transform a double to either 1 or 0
        env.insertFunction("bool", d -> d == 0 ? 0 : 1);
        // time-related
        env.insertFunction("now", () -> (double) System.currentTimeMillis()); //
        // constants
        env.insertVariable("pi", Math.PI);
        env.insertVariable("e", Math.E);
        env.insertVariable("tau", Math.TAU);
        env.insertVariable("true", 1);
        env.insertVariable("false", 0);

        // theoretical limit of Integer.MAX_VALUE parameters
        env.insertFunction("min", 2, Integer.MAX_VALUE, ctx -> {
            double min = ctx.getDouble(0);
            for (int i = 1; i < ctx.size(); i++) {
                double val = ctx.getDouble(i);
                if (val < min) {
                    min = val;
                }
            }
            return min;
        });
        env.insertFunction("max", 2, Integer.MAX_VALUE, ctx -> {
            double max = ctx.getDouble(0);
            for (int i = 1; i < ctx.size(); i++) {
                double val = ctx.getDouble(i);
                if (val > max) {
                    max = val;
                }
            }
            return max;
        });
        env.insertFunction("clamp", 3, ctx -> {
            double value = ctx.getDouble(0);
            double min = ctx.getDouble(1);
            double max = ctx.getDouble(2);
            Assert.isTrue(max >= min, "clamp: max must be greater than or equal to min");
            return (value > max) ? max : Math.max(value, min);
        });
        env.insertFunction("avg", 2, Integer.MAX_VALUE, ctx -> {
            double sum = 0;
            for (int i = 0; i < ctx.size(); i++) {
                sum += ctx.getDouble(i);
            }
            return sum / ctx.size();
        });
        env.insertFunction("sum", 2, Integer.MAX_VALUE, ctx -> {
            double sum = ctx.getDouble(0);
            for (int i = 1; i < ctx.size(); i++) {
                sum += ctx.getDouble(i);
            }
            return sum;
        });
        env.insertFunction("round", 1, 2, ctx -> {
            if (ctx.size() == 1) {
                return Math.round(ctx.getDouble(0));
            }
            double value = ctx.getDouble(0);
            int places = ctx.getInt(1);
            double factor = Utility.getPowerOfTen(places);

            return Math.round(value * factor) / factor;
        });
        env.insertFunction("rand", 0, 2, ctx -> {
            try {
                return switch (ctx.size()) {
                    case 0 -> RandomHolder.RANDOM.nextDouble();
                    case 1 -> RandomHolder.RANDOM.nextDouble(ctx.getDouble(0));
                    default -> RandomHolder.RANDOM.nextDouble(ctx.getDouble(0), ctx.getDouble(1));
                };
            } catch (IllegalArgumentException e) {
                // random throws this when f.e. the bound > the origin, translate this into a SyntaxException
                throw new SyntaxException(e.getMessage());
            }
        });
        env.insertFunction("gcd", 2, ctx -> {
            int a = ctx.getInt(0);
            int b = ctx.getInt(1);
            return Utility.gcd(a, b);
        });
        env.insertFunction("lcm", 2, ctx -> {
            int a = ctx.getInt(0);
            int b = ctx.getInt(1);
            return Utility.lcm(a, b);
        });

        return env;
    }

    static class RandomHolder {
        private static final SplittableRandom RANDOM = new SplittableRandom();
    }
}
