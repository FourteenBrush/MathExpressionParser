package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

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

    @Test
    void testPrecision() {
        assertEquals(117.62500501328793, ExpressionParser.parse("34.82726271 * 3.377383"));
        assertEquals(78.5052497661799, ExpressionParser.parse("182628161.938362528 % 253.938265272"));
        assertEquals(Double.POSITIVE_INFINITY, ExpressionParser.parse("12.928226*1111111111111111111111^7373"));
    }

    @Test
    void testMultipleOperatorsWithoutPriority() {
        assertEquals(3, ExpressionParser.parse("1+1+1"));
        assertEquals(8, ExpressionParser.parse("12-8+4"));
        assertEquals(1, ExpressionParser.parse("1-1+1"));
        assertEquals(9, ExpressionParser.parse("1-8+12+4"));
    }

    @Test
    void testMultipleOperatorsWithPriority() {
        assertEquals(720, ExpressionParser.parse("2*3*4*5*6"));
        assertEquals(0.25, ExpressionParser.parse("2/4/2"));
        assertEquals(57603, ExpressionParser.parse("3+4^5*(45*5)/4"));
        assertEquals(122, ExpressionParser.parse("34*12/4+4*5"));
    }

    @Test
    void testParentheses() {
        assertEquals(14, ExpressionParser.parse("2+(3*4)"));
        assertEquals(10, ExpressionParser.parse("(3+7)"));
        assertEquals(10, ExpressionParser.parse("((3+7))"));
        assertEquals(90, ExpressionParser.parse("((3+6)*10)"));
        assertEquals(30, ExpressionParser.parse("((3+6)*10)/3"));
        assertEquals(1, ExpressionParser.parse("(3*4)/12"));
        assertEquals(1, ExpressionParser.parse("((3*4)/(9+3))"));
    }

    @Test
    void testThrowingExpressions() {
        assertThrows(() -> ExpressionParser.parse(null));
        assertThrows(() -> ExpressionParser.parse("  "));
        assertThrows(() -> ExpressionParser.parse(" "));
        assertThrows(() -> ExpressionParser.parse("1 +"));
        assertThrows(() -> ExpressionParser.parse("+"));
        assertThrows(() -> ExpressionParser.parse("1. +"));
        assertThrows(() -> ExpressionParser.parse("+ 1"));
        assertThrows(() -> ExpressionParser.parse("1 + ."));
        assertThrows(() -> ExpressionParser.parse("-.1 + 1"));
        assertThrows(() -> ExpressionParser.parse("1."));
        assertThrows(() -> ExpressionParser.parse(".1"));
        assertThrows(() -> ExpressionParser.parse("1.."));
        assertThrows(() -> ExpressionParser.parse("."));
        assertThrows(() -> ExpressionParser.parse(".."));
        assertThrows(() -> ExpressionParser.parse("1..2"));
        assertThrows(() -> ExpressionParser.parse("1. -1"));
        assertThrows(() -> ExpressionParser.parse("1 + ."));
        assertThrows(() -> ExpressionParser.parse("1 + .1"));
        assertThrows(() -> ExpressionParser.parse("1 + 1."));
        assertThrows(() -> ExpressionParser.parse("1 * 1."));
        assertThrows(() -> ExpressionParser.parse("1. * 1"));
        assertThrows(() -> ExpressionParser.parse("1. * 1."));
        assertThrows(() -> ExpressionParser.parse("1.1 + 1 *"));
        assertThrows(() -> ExpressionParser.parse("(."));
        assertThrows(() -> ExpressionParser.parse(")"));
        assertThrows(() -> ExpressionParser.parse("1 + (1"));
        assertThrows(() -> ExpressionParser.parse("1 + 1)"));
        assertThrows(() -> ExpressionParser.parse("( )"));
        assertThrows(() -> ExpressionParser.parse("()"));
    }

    private void assertThrows(Executable executable) {
        Assertions.assertThrows(SyntaxException.class, executable);
    }
}