package me.fourteendoggo.mathexpressionparser.function;

import me.fourteendoggo.mathexpressionparser.Assert;
import me.fourteendoggo.mathexpressionparser.exceptions.FunctionNotFoundException;
import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

/**
 * A {@link FunctionContainer} is a data structure for storing functions and their solvers efficiently. <br/>
 * It uses a char trie to store the functions, where each node represents a character in the function name. <br/>
 * The root node doesn't have a value, and each node can have up to 26 children (a-z). <br/>
 * When you reach a {@link FunctionNode}, you know that you've found a function and the nodes you followed represent the function name. <br/>
 * The {@link FunctionNode} contains the handler for the function, however this node can still have children. <br/>
 * The data structure allows for efficient navigation in O(n) time, where n is the length of the function name.
 */
public class FunctionContainer {
    private final Node root = new Node(' ');

    public static FunctionContainer createDefault() {
        FunctionContainer container = new FunctionContainer();
        container.insertDefaultFunctions();
        return container;
    }

    private void insertDefaultFunctions() {
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
        insertFunction("abs", d -> Math.abs((int) d));

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

    /**
     * Inserts a function into this container.
     * This will return silently if the function already exists.
     * @param function the function to insert
     */
    public void insertFunction(FunctionCallSite function) {
        String functionName = function.getName();
        Node crawl = root;

        for (int i = 0; i < functionName.length() - 1; i++) { // last node must be a FunctionNode, hence the - 1
            char currentChar = functionName.charAt(i);
            crawl = insertChildReturnNext(currentChar, crawl, Node::new);
        }
        char lastChar = functionName.charAt(functionName.length() - 1);
        insertChildReturnNext(lastChar, crawl, current -> new FunctionNode(current, function));
    }

    private Node insertChildReturnNext(char currentChar, Node parent, CharFunction<Node> nodeFunction) {
        int index = currentChar - 'a';
        Assert.isTrue(index >= 0 && index <= 25, "function names must only contain lowercase letters");

        if (parent.children[index] == null) { // unnecessary for the last node, but it's fine
            parent.children[index] = nodeFunction.apply(currentChar);
        }
        return parent.children[index];
    }

    /**
     * Searches for a function in this container, starting at {@code buffer[fromPos]}
     * and assumes that the chars from that point on are the function name. <br/>
     * If this container would contain the function "sin" and "sinh", both will work. <br/>
     * If the function is found, the {@link FunctionCallSite} held by that node is returned. <br/>
     * @param buffer the buffer to search in
     * @param fromPos the position to start searching from, that position must be the first char of the function name
     * @return a {@link FunctionCallSite} if the function is found, always returns a non-null value
     * @throws FunctionNotFoundException if the function is not found
     * @throws SyntaxException if the function name does not match (a-z)
     */
    public FunctionCallSite search(char[] buffer, int fromPos) {
        Node crawl = root;

        for (; fromPos < buffer.length; fromPos++) {
            char current = buffer[fromPos];
            if (current == '(') break; // end of function name
            int index = current - 'a';
            Assert.isTrue(index >= 0 && index <= 26, "function names must only contain lowercase letters");

            Node child = crawl.children[index];
            Assert.notNull(child, "function not found", FunctionNotFoundException::new);

            crawl = child;
        }
        if (crawl instanceof FunctionNode fn) {
            return fn.handler;
        }
        throw new FunctionNotFoundException("only a function with similar name was found");
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
        private final FunctionCallSite handler;

        public FunctionNode(char value, FunctionCallSite handler) {
            super(value);
            this.handler = handler;
        }
    }

    @FunctionalInterface
    private interface CharFunction<R> {
        R apply(char c);
    }
}
