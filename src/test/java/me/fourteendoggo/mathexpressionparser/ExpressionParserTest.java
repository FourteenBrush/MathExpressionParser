package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpressionParserTest {

    @Test
    void testSimpleCalculations() {
        assertEquals(2, ExpressionParser.parse("1+1"));
        assertEquals(0, ExpressionParser.parse("1-1"));
        assertEquals(0, ExpressionParser.parse("-1 + 1"));
        assertEquals(0, ExpressionParser.parse("1 + -1"));
        assertEquals(-2, ExpressionParser.parse("-1 + -1"));
        assertEquals(2, ExpressionParser.parse("1 - -1"));
        assertEquals(1, ExpressionParser.parse("1 * 1"));
        assertEquals(1, ExpressionParser.parse("1 / 1"));
        assertEquals(-4, ExpressionParser.parse("-16 % 12"));
        assertEquals(4, ExpressionParser.parse("-2 * -2"));
        assertEquals(0, ExpressionParser.parse("0 *0"));
    }

    // TODO: overflow
    @Test
    void testPrecision() {
        assertEquals(117.62500501328793, ExpressionParser.parse("34.82726271 * 3.377383"));
        assertEquals(78.5052497661799, ExpressionParser.parse("182628161.938362528 % 253.938265272"));
        assertEquals(Double.POSITIVE_INFINITY, ExpressionParser.parse("12.928226*1111111111111111111111^7373"));
    }

    @Test
    void testThrowingExpressions() {
        assertThrows(null);
        assertThrows(" ");
        assertThrows("1 +");
        assertThrows("1 + .");
        assertThrows("1 + .1");
        assertThrows("1 + 1 +");
        assertThrows("+");
        assertThrows("1. + 1");
        assertThrows(" + 1");
        assertThrows("1. +");
        assertThrows(".1");
        assertThrows("1.");
        assertThrows("1..1");
        assertThrows("1..2");
        assertThrows("1..");
        assertThrows(".");
        assertThrows("..");
        assertThrows("1. -1");
        assertThrows("-.1 + 1");
        assertThrows("1. * 1");
        assertThrows("1 * 1.");
        assertThrows("1. * 1");
        assertThrows("1. * 1.");
        assertThrows("1.- * 1.");
        assertThrows("1.-1 * 1.-3");
        assertThrows("1.1 + 1*");
        assertThrows("(.");
        assertThrows(")");
        assertThrows("()");
        assertThrows("1 + (1");
        assertThrows("1+ 1)");
        assertThrows("((1+1)");
        assertThrows("(1+1))");
        assertThrows("( )");
        assertThrows("1.sin(1)");
        assertThrows("sin(1");
        assertThrows("sin 1");
        assertThrows("sin (1)");
        assertThrows("isn(1)");
        assertThrows("sin(1.cos(2))");
        assertThrows("sin(1.cos(2)");
        assertThrows("cos2(2)");
        assertThrows("max(0)"); // expects two args
        assertThrows("max(1,, 3)");
        assertThrows("max(1, )");
        assertThrows("max(1,)");
        assertThrows("max(,)");
        assertThrows("max(,,)");
        assertThrows("max((-))");
        assertThrows("min(max(1,3))");
    }

    private void assertThrows(String expression) {
        Assertions.assertThrows(SyntaxException.class, () -> ExpressionParser.parse(expression));
    }
}