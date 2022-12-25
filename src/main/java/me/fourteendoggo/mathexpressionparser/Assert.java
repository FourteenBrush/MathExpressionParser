package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;

import java.util.function.Function;

public interface Assert {

    static void notBlank(String input) {
        if (input.isBlank()) {
            throw new SyntaxException("cannot parse blank input");
        }
    }

    static void notNull(Object o, String message) {
        if (o == null) {
            throw new SyntaxException(message);
        }
    }

    static void notNull(Object o, String message, Function<String, SyntaxException> exceptionFunction) {
        if (o == null) {
            throw exceptionFunction.apply(message);
        }
    }

    static void isNull(Object o, String message) {
        if (o != null) {
            throw new SyntaxException(message);
        }
    }

    static void isFalse(boolean condition, String message) {
        if (condition) {
            throw new SyntaxException(message);
        }
    }

    static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new SyntaxException(message);
        }
    }
}
