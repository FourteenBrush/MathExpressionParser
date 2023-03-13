package me.fourteendoggo.mathexpressionparser.utils;

public class Utility {
    private static final int[] COMMON_POWERS_OF_TEN = {
            1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000
    };
    private static final String[] COMMON_ORDINAL_NAMES = {
            "first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eighth", "ninth", "tenth"
    };

    public static int getPowerOfTen(int exponent) {
        if (exponent < COMMON_POWERS_OF_TEN.length) {
            return COMMON_POWERS_OF_TEN[exponent];
        }
        return (int) Math.pow(10, exponent);
    }

    public static String getOrdinalName(int index) {
        if (index < COMMON_ORDINAL_NAMES.length) {
            return COMMON_ORDINAL_NAMES[index];
        }
        return (index + 1) + "th";
    }

    public static boolean isLowercaseLetter(int c) {
        return c >= 'a' && c <= 'z';
    }

    public static boolean isValidArgument(int c) {
        return c != ',' && c != ')';
    }

    public static boolean isBetweenBrackets(int c) {
        return c != ')';
    }
}
