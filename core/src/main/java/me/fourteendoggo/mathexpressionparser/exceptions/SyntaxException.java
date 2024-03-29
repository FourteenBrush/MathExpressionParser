package me.fourteendoggo.mathexpressionparser.exceptions;

public class SyntaxException extends RuntimeException {

    public SyntaxException(String message, Object... placeholders) {
        super(message.formatted(placeholders));
    }

    public SyntaxException(String message) {
        super(message);
    }
}
