package me.fourteendoggo.mathexpressionparser.container;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.utils.Assert;

import java.util.HashMap;
import java.util.function.IntPredicate;
import java.util.function.Supplier;

/**
 * A {@link CharTree} is a data structure used to map strings with an instance of type {@link T}. <br/>
 * A {@link HashMap} cannot be used in this case because we need a way to look up
 * a value based on a character array, a starting index and a predicate that tells what chars form a valid path. <br/>
 * In other words, we don't know the string and don't want to construct it first. <br/>
 * Both lookup and insertion are O(n) where n is the length of the string or the character array being searched through. <br/>
 */
public class CharTree<T> {
    private final IntPredicate inputValidator;
    private final Node root;

    /**
     * Creates a new {@link CharTree} with the given input validator, used to check whether a character is valid when inserting
     * or to stop searching at the current char when searching for an instance.
     * @param inputValidator a predicate that is used to check each character
     */
    public CharTree(IntPredicate inputValidator) {
        this.inputValidator = inputValidator;
        this.root = new Node(' ', inputValidator);
    }

    /**
     * Inserts a new value into the tree, possibly creating new nodes and maps the last node to the given value.
     * @param word the word to insert, as a char array, all the characters must match the input validator
     * @param instance the value to map to the last node
     * @throws SyntaxException if the input validator rejects any of the characters in the given word
     * or the last node with the value already exists
     */
    public void insert(char[] word, T instance) {
        Node crawl = root;

        for (int i = 0; i < word.length - 1; i++) {
            char current = word[i];
            crawl = crawl.putChildIfAbsent(current, () -> new Node(current, inputValidator));
        }
        char lastChar = word[word.length - 1];
        Node lastNode = crawl.putChild(lastChar, () -> new ValueHoldingNode<>(lastChar, inputValidator, instance));
        Assert.isFalse(lastNode instanceof ValueHoldingNode, "instance was already inserted");
    }

    /**
     * Searches for a word in the given buffer, starting at the given position. <br/>
     * The searching will stop when the end of the buffer is reached or the input validator rejects the current character. <br/>
     * @param buffer the buffer to search in
     * @param pos the starting index
     * @return the value found or null if no value was found
     * @throws SyntaxException if a valid character is not present as a path in this tree
     */
    @SuppressWarnings("unchecked")
    public T search(char[] buffer, int pos) {
        Node crawl = root;
        char current;
        while (pos < buffer.length && inputValidator.test(current = buffer[pos++])) {
            int index = current - 'a';
            Node child = crawl.children[index];
            Assert.notNull(child, "could not find node for character " + current);
            crawl = child;
        }
        if (crawl instanceof ValueHoldingNode<?> valueNode) {
            return (T) valueNode.heldValue;
        }
        return null;
    }

    @Override
    public String toString() {
        return "CharTree{root=" + root + '}';
    }

    private static class Node {
        private final char value;
        private final IntPredicate inputValidator;
        private final Node[] children;

        public Node(char value, IntPredicate inputValidator) {
            this.value = value;
            this.inputValidator = inputValidator;
            this.children = new Node[26];
        }

        public Node putChildIfAbsent(char value, Supplier<Node> nodeSupplier) {
            int index = indexOrThrow(value);
            if (children[index] == null) {
                children[index] = nodeSupplier.get();
            }
            return children[index];
        }

        public Node putChild(char value, Supplier<Node> nodeSupplier) {
            int index = indexOrThrow(value);
            Node oldValue = children[index];
            children[index] = nodeSupplier.get();
            return oldValue;
        }

        private int indexOrThrow(char value) {
            Assert.isTrue(inputValidator.test(value), "character '" + value + "' is not allowed to be inserted");
            return value - 'a';
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
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

    private static class ValueHoldingNode<T> extends Node {
        private final T heldValue;

        public ValueHoldingNode(char value, IntPredicate inputValidator, T heldValue) {
            super(value, inputValidator);
            this.heldValue = heldValue;
        }
    }
}
