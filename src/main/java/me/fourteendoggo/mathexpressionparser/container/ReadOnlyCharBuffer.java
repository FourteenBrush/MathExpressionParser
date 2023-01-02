package me.fourteendoggo.mathexpressionparser.container;

import me.fourteendoggo.mathexpressionparser.Assert;

import java.nio.BufferOverflowException;
import java.util.function.IntPredicate;

public class ReadOnlyCharBuffer {
    private final char[] array;
    private final IntPredicate loopCondition;
    private int position;

    public ReadOnlyCharBuffer(char[] array, IntPredicate loopCondition) {
        this.array = array;
        this.loopCondition = loopCondition;
    }

    public char[] array() {
        return array;
    }

    public int position() {
        return position;
    }

    public void position(int position) {
        this.position = position;
    }

    public void move(int amount) {
        if (position + amount > array.length) {
            throw new BufferOverflowException();
        }
        position += amount;
    }

    public boolean hasRemaining() {
        return hasRemainingUnchecked() && loopCondition.test(current());
    }

    public boolean hasRemainingUnchecked() {
        return position < array.length;
    }

    public char current() {
        return array[position];
    }

    public char get() {
        if (!hasRemainingUnchecked()) {
            throw new BufferOverflowException();
        }
        return array[position++];
    }

    public boolean hasAndGet(char expected) {
        if (hasRemainingUnchecked() && current() == expected) {
            position++;
            return true;
        }
        return false;
    }

    public void expectAndGet(char expected, String errorMessage) {
        Assert.isTrue(hasRemainingUnchecked() && get() == expected, errorMessage);
    }

    public void expectUnchecked(char expected, String errorMessage) {
        Assert.isTrue(hasRemainingUnchecked() && current() == expected, errorMessage);
    }

    @Override
    public String toString() {
        return new String(array, position, array.length - position);
    }
}
