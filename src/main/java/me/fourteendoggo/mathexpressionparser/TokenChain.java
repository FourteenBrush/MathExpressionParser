package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.tokens.Operand;
import me.fourteendoggo.mathexpressionparser.tokens.Operator;

public class TokenChain {
    // pointer represents the calculation we are currently constructing, acts as a tail
    private LinkedCalculation head, pointer;
    private TokenType lastType;
    private int numCalculations;

    public void addToken(Token token) {
        System.out.println("DEBUG: calling addToken(" + token + ")");
        validateIncomingType(token.getType());
        lastType = token.getType();

        if (pointer == null) { // would mean head is null too
            pointer = head = new LinkedCalculation((Operand) token);
        } else if (pointer.isComplete()) {
            System.out.println("DEBUG: pointer is complete, incoming token is " + token + ", pointer is " + pointer);
            pointer.next = new LinkedCalculation(pointer, (Operator) token);
            pointer = pointer.next; // move pointer to the newly created calculation
        } else {
            pointer.addToken(token);
            return; // we are adding to an existing calculation, no need to increment numCalculations
        }
        numCalculations++;
    }

    public void validateIncomingType(TokenType type) {
        if (lastType != null) {
            Validate.isTrue(lastType.canLink(type), "cannot add token of type %s after token of type %s", type, lastType);
        } else {
            Validate.isTrue(type == TokenType.OPERAND, "expected an operand as first token");
        }
    }

    public double solve() {
        if (numCalculations == 1) {
            return head.solveOrThrow();
        } else if (numCalculations == 2) {
            LinkedCalculation first = head;
            LinkedCalculation second = head.next;
            if (first.hasHigherPriority(second)) {
                // 3*4+5
                return second.operator.apply(first.solve(), second.right.getValue());
            } else {
                return first.operator.apply(second.solve(), first.left.getValue());
            }
        }

        // take calculations in pairs of two and compare their operator priorities
        LinkedCalculation first = head;
        LinkedCalculation second = head.next;
        while (second != null) {
            if (first.hasHigherPriority(second)) {
                // first one has a higher or the same priority, so we can solve it first
                // 3*4+5 -> [3*4] -> [4+5]
                double solvedValue = first.solve();
                second.left.setValue(solvedValue);
                if (first.prev != null) {
                    first.prev.right.setValue(solvedValue);
                    first.prev.next = second; // unlink ourselves from the chain
                }
            }
            first = first.next;
            second = second.next;
        }
        if (head != null) {
            double x = head.left.getValue();
            double y = first.solveOrThrow();
            return head.operator.apply(x, y);
        }
        return first.solveOrThrow();
    }

    public TokenType getLastType() {
        return lastType;
    }

    @Override
    public String toString() {
        if (numCalculations == 0) {
            return "[]";
        }

        StringBuilder builder = new StringBuilder(numCalculations * 15);
        builder.append('[');

        for (LinkedCalculation node = head; node != null; node = node.next) {
            if (builder.length() > 1) {
                builder.append(" -> ");
            }
            builder.append(node);
        }
        builder.append(']');
        return builder.toString();
    }

    /**
     * A linked list of calculations, dynamically constructed as tokens are added<br/>
     * Tokens are added linearly, so the list is always in the order of the input<br/>
     * Each calculation added to the {@link TokenChain} is supposed to have valid tokens,
     * only {@link TokenChain#pointer} may be incomplete, meaning it is still being constructed
     */
    private static class LinkedCalculation {
        private LinkedCalculation prev, next;
        private final Operand left;
        private Operand right;
        private Operator operator;

        public LinkedCalculation(Operand left) {
            this.left = left;
        }

        public LinkedCalculation(LinkedCalculation prev, Operator operator) {
            this(prev, prev.right);
            this.operator = operator;
        }

        public LinkedCalculation(LinkedCalculation prev, Operand left) {
            this.prev = prev;
            this.left = left;
        }

        public boolean hasHigherPriority(LinkedCalculation other) {
            return operator.getPriority() >= other.operator.getPriority();
        }

        public void addToken(Token token) {
            if (token instanceof Operand operand && right == null) {
                right = operand;
            } else if (token instanceof Operator op) {
                operator = op;
            }
            // ignore other cases, the token chain knows what it's doing
        }

        public boolean isComplete() {
            return operator != null && right != null; // left will never be null so no need to check
        }

        public double solve() {
            // no need to check if isComplete() is true, because this method is only called when it is
            return operator.apply(left, right);
        }

        public double solveOrThrow() {
            // left != null, operator != null, right != null
            if (isComplete()) { // give priority to solving the calculation rather than returning only the left operand
                return solve();
            } else if (operator == null) { // meaning right is null too
                return left.getValue();
            }
            throw new SyntaxException("did not expect anything of form 'x operator'");
        }

        @Override
        public String toString() {
            return '{' + left.getValue() + operator.getSymbol() + "" + right.getValue() + '}';
        }
    }
}
