package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;

public interface Validate {

    static void notBlank(String input) {
        if (input.isBlank()) {
            throw new SyntaxException("unexpected blank input");
        }
    }

    static void notNull(Object o, String message) {
        if (o == null) {
            throw new SyntaxException(message);
        }
    }

    static void notNull(Object o, String message, Object... placeholders) {
        if (o == null) {
            throw new SyntaxException(message.formatted(placeholders));
        }
    }

    static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new SyntaxException(message);
        }
    }

    static void isTrue(boolean condition, String message, Object... placeholders) {
        if (!condition) {
            throw new SyntaxException(message.formatted(placeholders));
        }
    }

    static void isFalse(boolean condition, String message) {
        if (condition) {
            throw new SyntaxException(message);
        }
    }
}
