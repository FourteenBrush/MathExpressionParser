package me.fourteendoggo.mathexpressionparser.symbol;

import me.fourteendoggo.mathexpressionparser.utils.Assert;
import me.fourteendoggo.mathexpressionparser.utils.Utility;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * An efficient lookup tree for {@link Symbol}s.
 */
public class SymbolLookup {
    private static final byte INVALID_IDX = Byte.MAX_VALUE;
    /** Last included ascii char of the range that {@link SymbolLookup#indexLookup} encompasses */
    private static final char MAX_RANGE_CHAR = 'z'; // (ascii value 122, 0x7A)
    /**
     * Stores indices into every {@link Node}'s children table, for the continuous ascii range that starts
     * at '\0' (NUL) and consists of the only valid ranges '0'..'9', 'A'..'Z' and 'a'..'z' (in that order).
     * <p>The fact that the range starts at '\0' means we store a leading range that is useless to us, as we only
     * really need to start at '0', this to avoid an extra start bounds check for inclusion of a certain character.
     * <p>Note: we also store useless ranges in between useful ranges to us, f.e. in between 'A'..'Z' and 'a'..'z',
     * all these useless positions are filled with {@link SymbolLookup#INVALID_IDX}, which is clearly invalid because it is
     * bigger than {@link Node#CHILDREN_WIDTH}.
     * <p>
     * Every index into this table is a character cast to an int, as per java requirements.
     * <p>Example:
     * <pre>{@code
     *     char symbolChar = 'a';
     *     // note that we don't need a start range check, because indexLookup starts at 0
     *     // this to benefit from the fact that java chars are unsigned.
     *     assert c <= MAX_RANGE_CHAR : "char is not contained within lookup table";
     *     int idx = indexLookup[c];
     *     assert idx != INVALID_IDX : "illegal char in symbol name";
     *     Node child = root.children[idx];
     *     assert child.value == symbolChar;
     * }</pre>
     */
    private static final byte[] indexLookup = new byte[MAX_RANGE_CHAR + 1];
    private final Node root = new Node(' ');

    static {
        //noinspection ConstantConditions
        if (INVALID_IDX <= Node.CHILDREN_WIDTH) {
            throw new AssertionError("static assertion failed: INVALID_IDX must not be less than " + Node.CHILDREN_WIDTH);
        }

        Arrays.fill(indexLookup, INVALID_IDX);

        /* relative start offsets for a certain range */
        final byte UPPERCASE_RANGE_START_OFFSET = 10; // ascii idx 65, child idx 10
        final byte LOWERCASE_RANGE_START_OFFSET = 26 + 10; // ascii idx 97, child idx 36

        for (char c = '0'; c <= '9'; c++) {
            indexLookup[c] = (byte) (c - '0');
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            indexLookup[c] = (byte) (c - 'A' + UPPERCASE_RANGE_START_OFFSET);
        }
        for (char c = 'a'; c <= 'z'; c++) {
            indexLookup[c] = (byte) (c - 'a' + LOWERCASE_RANGE_START_OFFSET);
        }
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

    /**
     * Looks up a {@link Symbol} in the given char buffer, starting at the given position.
     * @param buf the char buffer supplied by the tokenizer.
     * @param pos the position to start looking at.
     * @return a {@link Symbol} or null if not found.
     */
    public Symbol lookup(char[] buf, int pos) {
        // NOTE: buf[pos] points to the first char of the symbol, which is already considered valid
        // otherwise we wouldn't be here, no need for Utility.isValidIdentifierFirstChar()
        Node node = root;
        char current;

        while (pos < buf.length && Utility.isValidIdentifierChar(current = buf[pos++])) {
            int childIdx = indexLookup[current];
            node = node.children[childIdx];
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

    private static class Node {
        /**
         * Following the specification, the first char of an identifier must be alphabetical,
         * all following characters must be alphanumerical.
         * We are wasting some child nodes in the root node, but those cannot (not even accidentally) be used,
         * as guaranteed by the respective insert or lookup method in {@link SymbolLookup}.
         */
        private static final int CHILDREN_WIDTH = 10 + 26 + 26;
        private final char value; // for debugging only
        /* '0'..'9' 'A'..'Z' 'a'..'z' */
        private final Node[] children = new Node[CHILDREN_WIDTH];

        public Node(char value) {
            this.value = value;
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

        private static int indexOrThrow(char value) {
            Assert.isTrue(value < MAX_RANGE_CHAR, "character %s is not allowed in a symbol name", value);
            byte idx = indexLookup[value];
            Assert.isFalse(idx == INVALID_IDX, "character %s is not allowed in a symbol name", value);
            return idx;
        }

        // FIXME: more tree like string representation
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

    /**
     * A node that holds a {@link Symbol}, this node can still have child nodes.
     */
    private static class ValueHoldingNode extends Node {
        private final Symbol symbol;

        public ValueHoldingNode(char value, Symbol symbol) {
            super(value);
            this.symbol = symbol;
        }
    }
}
