package me.fourteendoggo.mathexpressionparser.symbol;

import me.fourteendoggo.mathexpressionparser.utils.Assert;
import me.fourteendoggo.mathexpressionparser.utils.Utility;
import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;

import java.util.Arrays;

/**
 * An efficient lookup tree for {@link Symbol}s.
 */
public class SymbolLookup {
    private static final byte INVALID_IDX = Byte.MAX_VALUE;
    /** Last included ascii char of the range that {@link SymbolLookup#indexLookup} encompasses */
    private static final char MAX_RANGE_CHAR = 'z'; // decimal 122
    /**
     * Stores a mapping between characters and indices into a {@link Node}'s children table. Every index into this table
     * is a character cast to an int. Every value is an absolute index into {@link Node#children}.
     * <p>This table encompasses the continuous ascii range that starts
     * at '\0' (NUL) and consists of the valid ranges '0'..'9', 'A'..'Z' and 'a'..'z' (in that order).
     * All values that are not in those three ranges are considered useless and have the value {@link SymbolLookup#INVALID_IDX}.
     * The fact that the whole range starts at '\0' is used to avoid an extra bounds check against the start index,
     * this to benefit from the fact that java chars are unsigned.
     * <p>Example:
     * <pre>{@code
     *     char symbolChar = 'a';
     *     // note that we don't need a start range check, because indexLookup starts at 0
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

        /* absolute start offsets into indexLookup; for a certain range */
        /* (previous range start + previous range width) */
        final byte UPPERCASE_RANGE_START_OFFSET = 10;
        final byte UNDERSCORE_OFFSET = UPPERCASE_RANGE_START_OFFSET + 26;
        final byte LOWERCASE_RANGE_START_OFFSET = UNDERSCORE_OFFSET + 1;

        for (char c = '0'; c <= '9'; c++) {
            indexLookup[c] = (byte) (c - '0');
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            indexLookup[c] = (byte) (c - 'A' + UPPERCASE_RANGE_START_OFFSET);
        }
        indexLookup['_'] = '_' - UNDERSCORE_OFFSET;
        for (char c = 'a'; c <= 'z'; c++) {
            indexLookup[c] = (byte) (c - 'a' + LOWERCASE_RANGE_START_OFFSET);
        }
    }

    /**
     * Inserts a symbol
     * @throws SyntaxException when this symbol already exists.
     */
    public void insert(Symbol symbol) {
        char[] chars = symbol.getName().toCharArray();
        Node node = root;

        for (int i = 0; i < chars.length - 1; i++) {
            char current = chars[i];
            node = node.getOrInsertChild(current);
        }

        char lastChar = chars[chars.length - 1];
        Node lastNode = node.insertValue(lastChar, symbol);
        Assert.isFalse(lastNode instanceof ValueHoldingNode, "symbol %s was already inserted", symbol.getName());
    }

    /**
     * Looks up a {@link Symbol} in the given char buffer, starting at the given position.
     * @param buf the char buffer supplied by the tokenizer.
     * @param pos the position to start looking at.
     * @return a {@link Symbol} or null if not found.
     */
    public Symbol lookup(char[] buf, int pos) {
        Node curr = root;
        do {
            int childIdx = indexLookup[buf[pos++]];
            curr = curr.children[childIdx];
            if (curr == null) return null;
        } while (pos < buf.length && Utility.isValidIdentifierChar(buf[pos]));

        if (curr instanceof ValueHoldingNode valueNode) {
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
         * all following characters must be alphanumerical or an underscore.
         * We are wasting some child nodes in the root node, but those cannot (not even accidentally) be used,
         * as guaranteed by the respective insert or lookup method in {@link SymbolLookup}.
         */
        private static final int CHILDREN_WIDTH = 10 + 26 + 1 + 26;
        /* '0'..'9' 'A'..'Z' '_' 'a'..'z' */
        private final Node[] children = new Node[CHILDREN_WIDTH];
        private final char value; // for debugging only

        public Node(char value) {
            this.value = value;
        }

        public Node getOrInsertChild(char value) {
            int idx = indexOrThrow(value);
            if (children[idx] == null) {
                children[idx] = new Node(value);
            }
            return children[idx];
        }

        public Node insertValue(char value, Symbol symbol) {
            int index = indexOrThrow(value);
            Node oldValue = children[index];
            // TODO: insert children copy
            children[index] = new ValueHoldingNode(value, symbol);
            return oldValue;
        }

        private static int indexOrThrow(char value) {
            Assert.isTrue(value <= MAX_RANGE_CHAR, "character %s is not allowed in a symbol name", value);
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
