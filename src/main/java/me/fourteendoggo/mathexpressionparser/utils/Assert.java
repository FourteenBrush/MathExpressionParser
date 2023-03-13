package me.fourteendoggo.mathexpressionparser.utils;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;

public interface Assert {

    static void notNull(Object o, String message) {
        if (o == null) {
            throw new SyntaxException(message);
        }
    }

    static void isFalse(boolean condition, String message, Object... placeholders) {
        if (condition) {
            throw new SyntaxException(message, placeholders);
        }
    }

    static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new SyntaxException(message);
        }
    }

    static void indexWithinBounds(int index, int size, String message, Object... placeholders) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(message.formatted(placeholders));
        }
    }
}
