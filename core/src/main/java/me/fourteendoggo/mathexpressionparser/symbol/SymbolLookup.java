package me.fourteendoggo.mathexpressionparser.symbol;

import me.fourteendoggo.mathexpressionparser.utils.Assert;
import me.fourteendoggo.mathexpressionparser.utils.Utility;

import java.util.function.IntPredicate;
import java.util.function.Supplier;

// TODO: allow more different chars as symbol name
public class SymbolLookup {
    private static final int CHILDREN_WIDTH = 26;
    private final IntPredicate characterValidator;
    private final Node root;

    public SymbolLookup() {
        characterValidator = Utility::isLowercaseLetter;
        root = new Node(' ');
    }

    public void insert(Symbol symbol) {
        Node node = root;
        char[] chars = symbol.getName().toCharArray();

        for (int i = 0; i < chars.length - 1; i++) {
            char current = chars[i];
            node = node.computeChildIfAbsent(current, () -> new Node(current));
        }

        char lastChar = chars[chars.length - 1];
        Node lastNode = node.insert(lastChar, new ValueHoldingNode(lastChar, symbol));
        Assert.isFalse(lastNode instanceof ValueHoldingNode, "symbol %s was already inserted", symbol.getName());
    }

    public Symbol lookup(char[] buf, int pos) {
        Node node = root;
        char current;
        while (pos < buf.length && characterValidator.test(current = buf[pos++])) {
            node = node.children[current - 'a'];
            if (node == null) return null;
        }
        if (node instanceof ValueHoldingNode valueNode) {
            return valueNode.symbol;
        }
        return null;
    }

    @Override
    public String toString() {
        return "SymbolLookup{root=" + root + '}';
    }

    private class Node {
        private final char value; // for debugging only
        private final Node[] children;

        public Node(char value) {
            this.value = value;
            this.children = new Node[CHILDREN_WIDTH];
        }

        public Node computeChildIfAbsent(char value, Supplier<Node> supplier) {
            int index = indexOrThrow(value);
            if (children[index] == null) {
                children[index] = supplier.get();
            }
            return children[index];
        }

        public Node insert(char value, Node node) {
            int index = indexOrThrow(value);
            Node oldValue = children[index];
            children[index] = node;
            return oldValue;
        }

        private int indexOrThrow(char value) {
            Assert.isTrue(characterValidator.test(value), "character " + value + " is not allowed to be used");
            return value - 'a';
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(100);
            sb.append(getClass().getSimpleName());
            sb.append("{'").append(value).append("', children=");

            for (int i = 0; i < children.length; i++) {
                if (children[i] == null) continue;
                if (i > 0 && children[i - 1] != null) {
                    sb.append(", ");
                }
                sb.append(children[i]);
            }
            // no children
            if (sb.charAt(sb.length() - 1) == '=') {
                sb.append("[]");
            }
            return sb.append('}').toString();
        }
    }

    private class ValueHoldingNode extends Node {
        private final Symbol symbol;

        public ValueHoldingNode(char value, Symbol symbol) {
            super(value);
            this.symbol = symbol;
        }
    }
}
