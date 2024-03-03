package me.fourteendoggo.mathexpressionparser.utils;

public class Utility {
    private static final int[] COMMON_POWERS_OF_TEN = {
            1, 10, 100, 1000, 10_000, 100_000, 1_000_000,
            10_000_000, 100_000_000, 1_000_000_000
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

    public static int requireInt(double x) {
        int intVal = (int) x;
        Assert.isTrue(x == intVal, "an integer is required");
        return intVal;
    }

    public static boolean isValidIdentifierFirstChar(char c) {
        c |= ' '; // set lowercase bit
        return c >= 'a' && c <= 'z';
    }

    // same as isValidIdentifierCharLowerCase but also handles uppercase chars
    public static boolean isValidIdentifierChar(char c) {
        if (c >= '0' && c <= '9') return true;
        c |= ' ';
        return (c >= 'a' && c <= 'z');
    }

    private static boolean isValidIdentifierCharLowerCase(char c) {
        return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9');
    }

    public static boolean isValidIdentifierName(String name) {
        if (name.isEmpty() || !isValidIdentifierFirstChar(name.charAt(0))) return false;
        for (int i = 1; i < name.length(); i++) {
            // numbers are immune to OR ' '
            char c = (char) (name.charAt(i) | ' ');
            if (!isValidIdentifierCharLowerCase(c)) return false;
        }
        return true;
    }

    public static double boolToDouble(boolean x) {
        return x ? 1 : 0;
    }

    public static boolean doubleToBool(double x) {
        return x != 0;
    }

    public static double boolAnd(double a, double b) {
        return (a != 0 && b != 0) ? 1 : 0;
    }

    public static double boolOr(double a, double b) {
        return (a != 0 || b != 0) ? 1 : 0;
    }

    public static double boolNot(double x) {
        return x == 0 ? 1 : 0;
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

    public static int gcd(int a, int b) {
        if (a < 0) a = -a;
        if (b < 0) b = -b;
        if (a < b) { // force a >= b
            int tmp = a;
            a = b;
            b = tmp;
        }

        while (b != 0) {
            int tmp = b;
            b = a % b;
            a = tmp;
        }
        return a;
    }

    public static int lcm(int a, int b) {
        if (b == 0) return 0;
        if (a < 0) a = -a;
        if (b < 0) b = -b;

        final int aCopy = a;
        final int bCopy = b;

        while (b != 0) {
            int tmp = b;
            b = a % b;
            a = tmp;
        }

        return aCopy / a * bCopy;
    }
}
