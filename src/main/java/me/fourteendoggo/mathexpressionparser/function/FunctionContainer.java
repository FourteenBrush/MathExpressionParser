package me.fourteendoggo.mathexpressionparser.function;

import me.fourteendoggo.mathexpressionparser.Assert;
import me.fourteendoggo.mathexpressionparser.exceptions.FunctionNotFoundException;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

/**
 * A {@link FunctionContainer} is a data structure for storing functions and their solvers efficiently. <br/>
 * It uses a char trie to store the functions, where each node represents a next character in the function name. <br/>
 * Starting from the root node (which doesn't have any value), each node can have up to 26 children (a-z). <br/>
 * When you reach a {@link FunctionNode}, you know that you found a function and the nodes you followed represent the function name. <br/>
 * The {@link FunctionNode} contains the solver for the function, this doesn't mean that they can't have children themselves. <br/>
 * The data structure allows for efficient navigation in O(n) time, where n is the length of the function name.
 */
public class FunctionContainer {
    private final Node root = new Node(' ');

    public FunctionContainer() {
        insertFunction("sin", Math::sin);
        insertFunction("cos", Math::cos);
        insertFunction("tan", Math::tan);
        insertFunction("asin", Math::asin);
        insertFunction("acos", Math::acos);
        insertFunction("atan", Math::atan);
        insertFunction("sinh", Math::sinh);
        insertFunction("cosh", Math::cosh);
        insertFunction("tanh", Math::tanh);

        insertFunction("sqrt", Math::sqrt);
        insertFunction("cbrt", Math::cbrt);
        insertFunction("log", Math::log);
        insertFunction("rad", Math::toRadians);
        insertFunction("abs", (double d) -> Math.abs((int) d));

        insertFunction(new FunctionCallSite("min", 2, Integer.MAX_VALUE, ctx -> {
            double min = ctx.get(0);
            for (int i = 1; i < ctx.size(); i++) {
                min = Math.min(min, ctx.get(i));
            }
            return min;
        }));
        insertFunction(new FunctionCallSite("max", 2, Integer.MAX_VALUE, ctx -> {
            double max = ctx.get(0);
            for (int i = 1; i < ctx.size(); i++) {
                max = Math.max(max, ctx.get(i));
            }
            return max;
        }));
        insertFunction(new FunctionCallSite("max", 3, ctx -> {
            double first = ctx.get(0);
            double second = ctx.get(1);
            double third = ctx.get(2);
            return Math.max(first, Math.max(second, third));
        }));
        insertFunction(new FunctionCallSite("pi", 0, ctx -> Math.PI));
    }

    public void insertFunction(String functionName, DoubleUnaryOperator function) {
        insertFunction(new FunctionCallSite(functionName, 1, ctx -> {
            double first = ctx.get(0);
            return function.applyAsDouble(first);
        }));
    }

    public void insertFunction(String functionName, DoubleBinaryOperator function) {
        insertFunction(new FunctionCallSite(functionName, 2, ctx -> {
            double first = ctx.get(0);
            double second = ctx.get(1);
            return function.applyAsDouble(first, second);
        }));
    }

    public void insertFunction(FunctionCallSite function) {
        root.insert(0, function);
    }

    public FunctionCallSite search(char[] function, int fromPos) {
        FunctionNode node = root.search(function, fromPos);
        return node.solver;
    }

    @Override
    public String toString() {
        return "CharTree{root=" + root + '}';
    }

    private static class Node {
        private final char value;
        private final Node[] children;

        public Node(char value) {
            this.value = value;
            this.children = new Node[26];
        }

        private void insert(int pos, FunctionCallSite solver) {
            String functionName = solver.getName();
            if (pos == functionName.length()) return; // to prevent out of bounds on recursive calls
            char current = functionName.charAt(pos);

            int index = current - 'a';
            Assert.isTrue(index >= 0 && index <= 26, "function names must only consist of lowercase letters");
            Node child = children[index];

            if (child == null) {
                if (pos == functionName.length() - 1) {
                    child = new FunctionNode(current, solver);
                } else {
                    child = new Node(current);
                }
                children[current - 'a'] = child;
            }
            child.insert(pos + 1, solver);
        }

        private FunctionNode search(char[] function, int fromPos) {
            if (fromPos == function.length || function[fromPos] == '(') {
                if (this instanceof FunctionNode fn) return fn;
                throw new FunctionNotFoundException("only a function with similar name exists");
            }
            int index = function[fromPos] - 'a';
            Assert.isTrue(index >= 0 && index <= 26, "function names must only consist of lowercase letters");

            Node child = children[index];
            Assert.notNull(child, "function not found", FunctionNotFoundException::new);
            return child.search(function, fromPos + 1);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (this instanceof FunctionNode) {
                sb.append("Function");
            }
            sb.append("Node{'").append(value).append("', children=");

            for (int i = 0; i < children.length; i++) {
                Node child = children[i];
                if (child == null) continue;
                if (i > 0 && children[i - 1] != null) {
                    sb.append(", ");
                }
                sb.append(child);
            }

            if (sb.charAt(sb.length() - 1) == '=') {
                sb.append("[]");
            }
            return sb.append('}').toString();
        }
    }

    private static class FunctionNode extends Node {
        private final FunctionCallSite solver;

        public FunctionNode(char value, FunctionCallSite solver) {
            super(value);
            this.solver = solver;
        }
    }
}
