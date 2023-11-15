package me.fourteendoggo.mathexpressionparser.symbol;

import me.fourteendoggo.mathexpressionparser.environment.ExecutionEnv;
import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.utils.Utility;

import java.util.SplittableRandom;

public abstract class BuiltinSymbols {
    private static final SplittableRandom RANDOM = new SplittableRandom();

    /**
     * Initializes the given {@link ExecutionEnv} with all the standard functions and variables
     * @param env the environment
     */
    public static void init(ExecutionEnv env) {
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
        env.insertFunction("now", () -> (double) System.currentTimeMillis()); // a long, hmm ye...
        // constants
        env.insertSymbol(new Variable("pi", Math.PI));
        env.insertSymbol(new Variable("e", Math.E));
        env.insertSymbol(new Variable("true", 1));
        env.insertSymbol(new Variable("false", 0));

        // theoretical limit of Integer.MAX_VALUE parameters
        env.insertFunction("min", 2, Integer.MAX_VALUE, ctx -> {
            double min = ctx.getDouble(0);
            for (int i = 1; i < ctx.size(); i++) {
                min = Math.min(min, ctx.getDouble(i));
            }
            return min;
        });
        env.insertFunction("max", 2, Integer.MAX_VALUE, ctx -> {
            double max = ctx.getDouble(0);
            for (int i = 1; i < ctx.size(); i++) {
                max = Math.max(max, ctx.getDouble(i));
            }
            return max;
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
                    case 0 -> RANDOM.nextDouble();
                    case 1 -> RANDOM.nextDouble(ctx.getDouble(0));
                    default -> RANDOM.nextDouble(ctx.getDouble(0), ctx.getDouble(1));
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
    }
}
