package me.fourteendoggo.mathexpressionparser;

public enum TokenType {
    OPERATOR,
    OPERAND,
    LEFT_PARENTHESIS,
    RIGHT_PARENTHESIS;

    public boolean canLink(TokenType other) {
        return switch (this) {
            case OPERATOR -> other == TokenType.OPERAND || other == TokenType.LEFT_PARENTHESIS;
            case OPERAND -> other == TokenType.OPERATOR || other == TokenType.LEFT_PARENTHESIS;
            case LEFT_PARENTHESIS -> other == TokenType.OPERAND;
            case RIGHT_PARENTHESIS -> other == TokenType.OPERATOR;
        };
    }
}
