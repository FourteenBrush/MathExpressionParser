package me.fourteendoggo.mathexpressionparser.token;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.utils.Assert;

/**
 * Represents a solvable expression being constructed.
 * @see Expression#solve()
 */
public class Expression {
    private LinkedCalculation head, tail;
    private TokenType lastType = TokenType.OPERATOR; // need to assure incoming type is different from the current one
    private int numCalculations;

    /**
     * Pushes an {@link Operand}
     */
    public void pushToken(double value) {
        pushToken(new Operand(value));
    }

    public void pushToken(Token token) {
        checkType(token.getType());
        lastType = token.getType();

        if (tail == null) {
            head = tail = new LinkedCalculation((Operand) token);
            numCalculations++;
            return;
        }

        // if we could not reuse the last operand of tail, allocate a new tail
        if (tail.isComplete() && !tail.simplify()) {
            tail.next = new LinkedCalculation(tail, tail.right, (Operator) token);
            tail = tail.next;
            numCalculations++;
        } else {
            tail.pushToken(token);
        }
    }

    private void checkType(TokenType type) {
        if (lastType != type) return;

        throw new SyntaxException(switch (type) {
            case OPERAND -> "expected operator, got operand";
            case OPERATOR -> "expected operand, got operator";
        });
    }

    /**
     * Solves the expression represented by this token list. <br/>
     * Simple expressions like 1+1 or 1*3+1 will be solved in one pass.
     * <h2>Algorithm explained:</h2>
     * The algorithm works with a linked list of calculations, each calculation object is a
     * wrapper for an operator and its surrounding operands, this causes overlapping with operands but is perfectly fine. <br/>
     * <p/>
     * To solve, we start at the head of the list and take two calculations. <br/>
     * If the first calculation has an operator with a higher or the same priority,
     * we can safely solve the first calculation and set its result to the left of the second calculation
     * and unlink that first calculation. <br/>
     * For example: <br/>
     * <p/>
     * Expression before: 3 * 4 + 2 <br/>
     * Transformed to calculations: [3 * 4] -> [4 + 2] <br/>
     * Resulting calculation after applying this algorithm would be: [12 + 2] (left calculation could be solved).
     * <p/>
     * This algorithm is repeated until there is only one calculation left, which is then solved and returned. <br/>
     * This might involve looping multiple times over the expression, keeping higher priority operators
     * at the beginning reduces this small 'overhead' because the first calculation can always be solved.
     *
     * @return the result of the expression
     */
    public double solve() {
        return switch (numCalculations) {
            case 0 -> throw new SyntaxException("cannot solve an empty expression");
            case 1 -> head.tryToSolve();
            case 2 -> {
                LinkedCalculation first = head;
                LinkedCalculation second = first.next;

                Assert.notNull(second.right, "unexpected trailing operator");

                if (first.canExecuteFirst()) {
                    // f.e. 2*3+2
                    double firstOperand = first.solve();
                    double secondOperand = second.right.getValue();
                    yield second.operator.apply(firstOperand, secondOperand);
                }
                // f.e. 2+3*2
                double firstOperand = first.left.getValue();
                double secondOperand = second.solve();
                yield first.operator.apply(firstOperand, secondOperand);
            }
            default -> {
                shorten();
                yield head.solve();
            }
        };
    }

    /**
     * Shortens a chain of calculations, leaves one calculation behind which can then be solved.
     */
    private void shorten() {
        while (numCalculations > 1) {
            for (LinkedCalculation current = head; current != null; current = current.next) {
                LinkedCalculation next = current.next;
                if (next != null) {
                    // we can only solve 'current' if its operator priority is higher than or equal to the next's operator priority
                    if (!current.canExecuteFirst()) continue;
                    // unlink current
                    LinkedCalculation prev = current.prev;
                    double solved = current.solve();
                    next.left.setValue(solved);
                    //next.left.setValue(current.solve());
                    next.prev = prev; // remove traces to us on next
                    numCalculations--;
                    // we may have a left neighbour which didn't get executed yet because its operator priority is lower than ours
                    if (prev == null) {
                        // looks like we are head, and we are about to be unlinked, change head to next
                        head = next;
                    } else {
                        prev.right.setValue(solved);
                        prev.next = next;
                    }
                } else if (current.prev != null) {
                    // current refers to the tail
                    // append our value to prev.right
                    current.prev.right.setValue(current.solve());
                    // unlink current
                    current.prev.next = null;
                    current.prev = null;
                    numCalculations--;
                }
            }
        }
    }

    /**
     * @return the {@link TokenType} of the last token pushed, or {@link TokenType#OPERATOR}
     */
    public TokenType getLastType() {
        return lastType;
    }

    @Override
    public String toString() {
        if (numCalculations == 0) {
            return "[]";
        }

        StringBuilder builder = new StringBuilder();
        for (LinkedCalculation node = head; node != null; node = node.next) {
            if (builder.length() > 1) {
                builder.append(" -> ");
            }
            builder.append(node);
        }
        return builder.toString();
    }

    /**
     * A linked list of calculations, dynamically constructed as tokens are added.<br/>
     * Tokens are added linearly, so the list is always in the order of the input.<br/>
     * Each calculation added to the {@link Expression} is supposed to have valid tokens,
     * only {@link Expression#tail} may be incomplete, meaning it is still being constructed
     */
    private static class LinkedCalculation {
        private LinkedCalculation prev, next;
        private final Operand left;
        private Operand right;
        private Operator operator;

        public LinkedCalculation(LinkedCalculation prev, Operand left, Operator operator) {
            this.prev = prev;
            this.left = left;
            this.operator = operator;
        }

        public LinkedCalculation(Operand left) {
            this.left = left;
        }

        // we have a reference to the next calculation so no need to implement Comparable<LinkedCalculation>
        public boolean canExecuteFirst() {
            return operator.getPriority() >= next.operator.getPriority();
        }

        /**
         * Pushes a token to this calculation object.
         * The caller should check {@link #isComplete()} before, to ensure they don't overwrite the same fields again,
         * as this just sets fields and doesn't check anything.
         * @param token the token to push, either an operand or an operator
         */
        public void pushToken(Token token) {
            switch (token.getType()) {
                case OPERAND -> right = (Operand) token;
                case OPERATOR -> operator = (Operator) token;
            }
        }

        /**
         * @return true if this calculation is complete and can be solved
         */
        public boolean isComplete() {
            return operator != null && right != null; // left will never be null so no need to check
        }

        /**
         * Tries to simplify this calculation, effectively checking if its operator priority is {@link Operator#HIGHEST_PRIORITY}. <br>
         * Then solving the calculation and marking it as "incomplete" again to allow further adding of tokens.
         * @return true, if this calculation could be simplified, false otherwise
         */
        public boolean simplify() {
            if (operator.getPriority() != Operator.HIGHEST_PRIORITY) {
                return false;
            }
            double result = solve();
            left.setValue(result);
            operator = null;
            right = null;
            return true;
        }

        public double solve() {
            return operator.apply(left, right);
        }

        // some support to work with incomplete calculations f.e. [3,null,null] (3)
        /**
         * Either solves this calculation if complete, or returns the left operand if that's the only thing set,
         * throws otherwise
         * @return the first operands value, or the solved expressions value
         * @throws SyntaxException if we hold both a left operand and an operator (how even would we solve that?)
         */
        public double tryToSolve() {
            // illegal state of having a left operand and right operand but no operator can never occur
            if (operator == null) {
                return left.getValue();
            }
            Assert.notNull(right, "unexpected trailing operator");
            return solve();
        }

        @Override
        public String toString() {
            if (operator == null) { // right also null
                return "[" + left + "]";
            } else if (right == null) { // only right null
                return "[" + left + ", " + operator.getSymbol() + "]";
            }
            return "[" + left + ", " + operator + ", " + right + "]";
        }
    }
}
