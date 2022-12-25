package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.tokens.Operand;
import me.fourteendoggo.mathexpressionparser.tokens.Operator;

public class TokenList {
    // pointer represents the calculation we are currently constructing, acts as a tail
    private LinkedCalculation head, pointer;
    private TokenType lastType;
    private int numCalculations;

    public void addToken(Token token) {
        validateIncomingType(token.getType());
        lastType = token.getType();

        if (pointer == null) {
            LinkedCalculation calculation = new LinkedCalculation((Operand) token);
            pointer = calculation;
            head = calculation;
            numCalculations++;
        } else if (pointer.isComplete()) {
            if (pointer.simplify()) {
                // pointer only contains a left operand and an operator now
                pointer.addToken(token);
            } else {
                // pointer is a complete calculation, but it was not simplified
                pointer.next = new LinkedCalculation(pointer, (Operator) token);
                pointer = pointer.next;
                numCalculations++;
            }
        } else {
            pointer.addToken(token);
        }
    }

    public void validateIncomingType(TokenType type) {
        if (lastType != null) {
            if (lastType.canLink(type)) return;

            String firstTokenName = translate(type);
            String secondTokenName = translate(lastType);
            throw new SyntaxException("cannot add " + firstTokenName + " after " + secondTokenName);
        } else {
            if (type == TokenType.OPERAND || type == TokenType.LEFT_PARENTHESIS) return;

            String receivedToken = translate(type);
            throw new SyntaxException("expected operand or left parentheses as first token, got " + receivedToken);
        }
    }

    private String translate(TokenType type) {
        return type.name().toLowerCase().replace('_', ' ');
    }

    /**
     * Solves the expression formed by this token list <br/>
     * Simple expressions like 1+1 or 1*3+1 will be solved in one pass
     * <h2>Algorithm explained:</h2>
     * The algorithm works with a linked list of calculations, each calculation object is a
     * wrapper for an operator and its surrounding operands, this causes overlapping with operands but is perfectly fine <br/>
     * <p/>
     * To solve, we start at the head of the list and take two calculations <br/>
     * If the first calculation has an operator with a higher or the same priority
     * we can safely solve the first calculation and apply its result to the left of the second calculation
     * and unlink that first calculation<br/>
     * Visually this would look like this: <br/>
     * <p/>
     * Expression before: 2 + 3 * 4 <br/>
     * Transformed to calculations: [2+3] -> [3*4] <br/>
     * Result calculation after applying this algorithm would be: [2+12]
     * <p/>
     * This algorithm is repeated until there is only one calculation left, which is then solved and returned <br/>
     * This might involve looping multiple times over the expression, keeping higher priority operators
     * at the beginning reduces this overhead because the first calculation can always be solved
     *
     * @return the result of the expression
     */
    public double solve() {
        return switch (numCalculations) {
            case 1 -> head.solveOrThrow();
            case 2 -> {
                LinkedCalculation first = head;
                LinkedCalculation second = first.next;

                if (first.mayExecuteFirst()) {
                    // 2*3+2
                    double leftOperand = first.solve();
                    double secondOperand = second.right.getValue();
                    yield second.operator.apply(leftOperand, secondOperand);
                }
                // 2+3*2
                double firstOperand = first.left.getValue();
                double secondOperand = second.solve();
                yield first.operator.apply(firstOperand, secondOperand);
            }
            default -> {
                // throw an exception for '()'
                Assert.isFalse(numCalculations == 0, "cannot solve an empty expression");
                while (numCalculations > 1) {
                    shorten();
                }
                yield head.solve();
            }
        };
    }

    private void shorten() {
        // lets take {2+3}<->{3*4}<->{4+2} as example (expression: 2+3*4+2) and {3*4} as current
        for (LinkedCalculation current = head; current != null; current = current.next) {
            LinkedCalculation next = current.next;
            if (next != null) {
                // we can only solve 'current' if its operator priority is higher than or equal to the next's operator priority
                if (!current.mayExecuteFirst()) continue;
                // at this point we want to unlink 'current'
                // prepare to remove 'current' from the list, this means placing its solved value in next.left and prev.right
                double solvedValue = current.solve();
                LinkedCalculation prev = current.prev;
                // {2+3}<->{3*4}<->{12+2}
                next.left.setValue(solvedValue);
                next.prev = prev; // remove traces to us on next
                numCalculations--;
                // we may have a left neighbour which didn't get executed yet because its operator priority is lower than ours
                if (prev == null) {
                    // looks like we are head, and we are about to be unlinked, change head to next just in case we fuck up
                    head = next;
                    continue;
                }

                // {2+12}<->{3*4}<->{4+2}
                prev.right.setValue(solvedValue);
                prev.next = next; // remove traces of us on prev
                // prev.next points to next
                // next.prev points to current.prev
            } else if (current.prev != null) {
                // current refers to the tail
                // append our value to prev.right
                double solvedValue = current.solve();
                current.prev.right.setValue(solvedValue);
                // unlink current
                current.prev.next = null;
                numCalculations--;
            }
        }
    }

    public TokenType getLastType() {
        return lastType;
    }

    @Override
    public String toString() {
        if (numCalculations == 0) {
            return "[]";
        }

        StringBuilder builder = new StringBuilder(numCalculations * 15); // whatever
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
     * Each calculation added to the {@link TokenList} is supposed to have valid tokens,
     * only {@link TokenList#pointer} may be incomplete, meaning it is still being constructed
     */
    private static class LinkedCalculation {
        private LinkedCalculation prev, next;
        private final Operand left;
        private Operand right;
        private Operator operator;

        public LinkedCalculation(LinkedCalculation prev, Operator operator) {
            this(prev, prev.right);
            this.operator = operator;
            // might want to call clone() on prev.right because Operand is mutable
        }

        public LinkedCalculation(LinkedCalculation prev, Operand left) {
            this(left);
            this.prev = prev;
        }

        public LinkedCalculation(Operand left) {
            this.left = left;
        }

        // we have a reference to the next calculation so no need for a method that takes in another calculation object
        public boolean mayExecuteFirst() {
            return operator.getPriority() >= next.operator.getPriority();
        }

        public void addToken(Token token) {
            // no need to check if right == null because isComplete is checked first
            switch (token.getType()) {
                case OPERAND -> right = (Operand) token;
                case OPERATOR -> operator = (Operator) token;
            }
            // ignore other cases
        }

        public boolean isComplete() {
            return operator != null && right != null; // left will never be null so no need to check
        }

        public boolean simplify() {
            if (operator.getPriority() != Operator.HIGHEST_PRIORITY) {
                return false;
            }
            double result = solve();
            left.setValue(result);
            right = null;
            return true;
        }

        public double solve() {
            // no need to check if isComplete() is true, because this method is only called when it is
            return operator.apply(left, right);
        }

        // some support to work with incomplete calculations f.e. [3,null,null] (3)
        public double solveOrThrow() {
            // left != null, operator != null, right != null
            if (isComplete()) { // give priority to solving the calculation rather than returning only the left operand
                return solve();
            }
            Assert.isNull(operator, "unable to solve calculation of form 'x operator'");

            return left.getValue();
        }

        @Override
        public String toString() {
            return "{" + left.getValue() + ',' +
                    (operator != null ? operator.getSymbol() : "null") + ',' +
                    (right != null ? right.getValue() : "null") + '}';
        }
    }
}
