package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.tokens.Operand;
import me.fourteendoggo.mathexpressionparser.tokens.Operator;

public class TokenList {
    private Node head, tail;
    private int size;
    private TokenType lastType;

    public TokenType getLastType() {
        return lastType;
    }

    public void testNextType(TokenType type) {
        if (lastType == null) return;

        Validate.isTrue(lastType.canLink(type), "cannot add token of type %s after token of type %s", type, lastType);
    }

    /**
     * Adds the specified token to the end of the list
     * A check will be performed first to see if the previous token can be linked to the new one
     * @param token the token to add
     * @throws SyntaxException if the token was not expected
     */
    public void addToken(Token token) {
        testNextType(token.getType());
        lastType = token.getType();

        Node node = new Node(token);
        if (head == null) {
            head = node;
        } else if (tail == null) {
            tail = node;
            head.next = tail;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
    }

    public void clear() {
        head = tail = null;
        lastType = null;
        size = 0;
    }

    public double calculateAndClear() {
        if (size == 1) {
            return ((Operand) head.token).getValue(); // handling parentheses
        }

        Validate.isTrue(size >= 3, "not enough tokens to calculate");
        // gather calculations in the form of "x operator y" and calculate them
        // note that operator priority is not taken into account
        Operand firstOperand = null;
        Operand secondOperand = null;
        Operator operator = null;

        for (Node node = head; node != null; node = node.next) {
            Token token = node.token;
            if (token instanceof Operand operand) {
                if (firstOperand == null) {
                    firstOperand = operand;
                } else if (secondOperand == null) {
                    secondOperand = operand;
                } else {
                    throw new SyntaxException("expected an operator");
                }
            } else if (token instanceof Operator op && operator == null) {
                operator = op;
            } else {
                throw new SyntaxException("unimplemented type"); // parentheses or whatever other unimplemented type
            }

            if (firstOperand != null && secondOperand != null && operator != null) {
                double result = operator.apply(firstOperand, secondOperand);
                firstOperand.setValue(result);
                secondOperand = null;
                operator = null;
            }
        } // TODO implement operator priority
        Validate.notNull(firstOperand, "first operand was null"); // is this really needed?
        clear();

        return firstOperand.getValue();
    }

    @Override
    public String toString() {
        if (size == 0) {
            return "[]";
        }

        StringBuilder builder = new StringBuilder(size * 15);
        builder.append('[');
        for (Node node = head; node != null; node = node.next) {
            if (builder.length() > 1) {
                builder.append(", ");
            }

            builder.append(node.token);
        }
        builder.append(']');
        return builder.toString();
    }

    private static class Node {
        private final Token token;
        private Node next;

        public Node(Token token) {
            this.token = token;
        }
    }
}
