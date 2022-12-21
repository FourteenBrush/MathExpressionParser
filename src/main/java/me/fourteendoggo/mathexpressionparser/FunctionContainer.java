package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.exceptions.FunctionNotFoundException;
import me.fourteendoggo.mathexpressionparser.tokens.FunctionCallSite;

import java.util.function.ToDoubleFunction;

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
        System.out.println("FunctionContainer initialized");
        insert("sin", args -> Math.sin(args[0]));
        insert("cos", args -> Math.cos(args[0]));
        insert("tan", args -> Math.tan(args[0]));
        insert("asin", args -> Math.asin(args[0]));
        insert("acos", args -> Math.acos(args[0]));
        insert("atan", args -> Math.atan(args[0]));
        insert("sinh", args -> Math.sinh(args[0]));
        insert("cosh", args -> Math.cosh(args[0]));
        insert("tanh", args -> Math.tanh(args[0]));
        insert("tanh", args -> Math.tanh(args[0]));
        insert("sqrt", args -> Math.sqrt(args[0]));
        insert("rad", args -> Math.toRadians(args[0]));
        insert("log", args -> Math.log(args[0]));
    }

    public void insert(String function, ToDoubleFunction<double[]> solver) {
        root.insert(function.trim(), 0, solver);
    }

    public FunctionNode search(char[] function, int fromPos) {
        return root.search(function, fromPos);
    }

    public FunctionCallSite searchFunctionNode(char[] function, int fromPos) {
        FunctionNode node = root.search(function, fromPos);
        return new FunctionCallSite(function.length, node.solver);
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

        private void insert(String function, int pos, ToDoubleFunction<double[]> solver) {
            if (pos == function.length()) return; // to prevent out of bounds on recursive calls
            char current = function.charAt(pos);
            Node child = children[current - 'a'];
            if (child == null) {
                if (pos == function.length() - 1) {
                    child = new FunctionNode(function.length(), current, solver);
                } else {
                    child = new Node(current);
                }
                children[current - 'a'] = child;
            }
            child.insert(function, pos + 1, solver);
        }

        private FunctionNode search(char[] function, int fromPos) {
            if (fromPos == function.length || function[fromPos] == '(') {
                if (this instanceof FunctionNode fn) return fn;
                throw new FunctionNotFoundException("only a function with similar name exists");
            }
            Node child = children[function[fromPos] - 'a'];
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

    public static class FunctionNode extends Node {
        private final int length;
        private final ToDoubleFunction<double[]> solver;

        private FunctionNode(int length, char value, ToDoubleFunction<double[]> solver) {
            super(value);
            this.length = length;
            this.solver = solver;
        }

        public int getLength() {
            return length;
        }

        public double apply(double... args) {
            return solver.applyAsDouble(args);
        }
    }
}
