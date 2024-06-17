package me.fourteendoggo.mathexpressionparser.function;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.DoubleStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FunctionContextTest {
    private FunctionContext ctx;

    @BeforeEach
    void setUp() {
        ctx = new FunctionContext();
    }

    @Test
    void testEmptyContext() {
        assertThat(ctx.isEmpty()).isTrue();
        assertThat(ctx.size()).isZero();

        assertThatThrownBy(() -> ctx.getDouble(0)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> ctx.getDouble(-1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> ctx.getInt(0)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> ctx.getInt(-3)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void testLargeNumberOfParameters() {
        DoubleStream.generate(() -> 1.3).limit(2000)
                .forEach(ctx::add);

        assertThat(ctx.size()).isEqualTo(2000);
        assertThatThrownBy(() -> ctx.getInt(1999))
                .isInstanceOf(SyntaxException.class)
                // TODO: maybe we want it to be zero based?
                .hasMessage("expected an integer as 2000th argument, got 1.3");
    }

    @Test
    void testBoundsAndConversions() {
        ctx.add(2.3);
        assertThat(ctx.getDouble(0)).isEqualTo(2.3);
        assertThatThrownBy(() -> ctx.getDouble(-1)).isInstanceOf(IndexOutOfBoundsException.class);

        // conversions
        assertThatThrownBy(() -> ctx.getInt(0)).isInstanceOf(SyntaxException.class);
        assertThatThrownBy(() -> ctx.getBoundedDouble(0, -10, 2)).isInstanceOf(SyntaxException.class);

        assertThatThrownBy(() -> ctx.getBoundedDouble(0, 10, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("set up incorrectly");
    }

    @Test
    void test_getUnsigned() {
        assertThatThrownBy(() -> ctx.getUnsignedInt(0)).isInstanceOf(IndexOutOfBoundsException.class);

        ctx.add(2.1);
        assertThat(ctx.getUnsignedDouble(0)).isEqualTo(2.1);
        assertThatThrownBy(() -> ctx.getUnsignedInt(0))
                .isInstanceOf(SyntaxException.class)
                .hasMessageContaining("expected an integer as first argument");

        assertThatThrownBy(() -> ctx.getUnsignedInt(-1)).isInstanceOf(IndexOutOfBoundsException.class);

        ctx.add(-12);
        assertThatThrownBy(() -> ctx.getUnsignedInt(1)).isInstanceOf(SyntaxException.class)
                .hasMessageContaining("expected an integer between");
    }

    @Test
    void testStream() {
        ctx.add(2.3);
        ctx.add(1.2);
        assertThat(ctx.stream().toArray()).containsExactly(2.3, 1.2);
    }

    @Test
    void testEmptyStream() {
        assertThat(ctx.stream().toArray()).isEmpty();
    }
}