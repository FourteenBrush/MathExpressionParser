package me.fourteendoggo.mathexpressionparser.function;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}