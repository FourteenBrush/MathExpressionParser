package me.fourteendoggo.mathexpressionparser.utils;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;

// inline those functions if java will ever allow us to
public interface Assert {

    static void notNull(Object o, String message) {
        if (o == null) {
            throw new SyntaxException(message);
        }
    }

    static void isFalse(boolean condition, String fmt, Object... placeholders) {
        if (condition) {
            throw new SyntaxException(fmt, placeholders);
        }
    }

    static void isTrue(boolean condition, String fmt, Object... placeholders) {
        if (!condition) {
            throw new SyntaxException(fmt.formatted(placeholders));
        }
    }

    static void indexWithinBounds(int idx, int size, String fmt, Object... placeholders) {
        if (idx < 0 || idx >= size) {
            throw new IndexOutOfBoundsException(fmt.formatted(placeholders));
        }
    }

    static void isValidIdentifierName(String name) {
        if (!Utility.isValidIdentifierName(name)) {
            throw new SyntaxException("invalid identifier name: " + name);
        }
    }
}
