package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.*;

class ExpressionParserTest {

    @ParameterizedTest
    @CsvFileSource(resources = "/positive-input.csv")
    void testPositiveTestCases(String expression, String expectedResult) {
        double expected = ExpressionParser.parse(expectedResult);
        double result = assertDoesNotThrow(() -> ExpressionParser.parse(expression));
        // delta 0 to make 0.0 == -0.0 pass, junit uses Double.doubleToLongBits for comparison
        assertEquals(expected, result, 0, () -> "Got %f instead of %f".formatted(result, expected));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/negative-input.csv")
    void testNegativeTestCases(String expression) {
        assertThrows(SyntaxException.class, () -> ExpressionParser.parse(expression));
    }

    @Test
    void testThrowingExpressions() {
        assertThrows(NullPointerException.class, () -> ExpressionParser.parse(null));
        assertThrows(SyntaxException.class, () -> ExpressionParser.parse(""));
    }

    @Test
    void testBooleanTruthTables() {
        // AND
        assertEquals(0, ExpressionParser.parse("and(0, 0)"));
        assertEquals(0, ExpressionParser.parse("and(0, 1)"));
        assertEquals(0, ExpressionParser.parse("and(1, 0)"));
        assertEquals(1, ExpressionParser.parse("and(1, 1)"));

        assertEquals(0, ExpressionParser.parse("0 && 0"));
        assertEquals(0, ExpressionParser.parse("0 && 1"));
        assertEquals(0, ExpressionParser.parse("1 && 0"));
        assertEquals(1, ExpressionParser.parse("1 && 1"));
        // NAND
        assertEquals(1, ExpressionParser.parse("nand(0, 0)"));
        assertEquals(1, ExpressionParser.parse("nand(0, 1)"));
        assertEquals(1, ExpressionParser.parse("nand(1, 0)"));
        assertEquals(0, ExpressionParser.parse("nand(1, 1)"));

        assertEquals(1, ExpressionParser.parse("!(0 && 0)"));
        assertEquals(1, ExpressionParser.parse("!(0 && 1)"));
        assertEquals(1, ExpressionParser.parse("!(1 && 0)"));
        assertEquals(0, ExpressionParser.parse("!(1 && 1)"));
        // OR
        assertEquals(0, ExpressionParser.parse("or(0, 0)"));
        assertEquals(1, ExpressionParser.parse("or(0, 1)"));
        assertEquals(1, ExpressionParser.parse("or(1, 0)"));
        assertEquals(1, ExpressionParser.parse("or(1, 1)"));

        assertEquals(0, ExpressionParser.parse("0 || 0"));
        assertEquals(1, ExpressionParser.parse("0 || 1"));
        assertEquals(1, ExpressionParser.parse("1 || 0"));
        assertEquals(1, ExpressionParser.parse("1 || 1"));
        // XOR
        assertEquals(0, ExpressionParser.parse("xor(0, 0)"));
        assertEquals(1, ExpressionParser.parse("xor(0, 1)"));
        assertEquals(1, ExpressionParser.parse("xor(1, 0)"));
        assertEquals(0, ExpressionParser.parse("xor(1, 1)"));

        assertEquals(0, ExpressionParser.parse("0 ^ 0"));
        assertEquals(1, ExpressionParser.parse("0 ^ 1"));
        assertEquals(1, ExpressionParser.parse("1 ^ 0"));
        assertEquals(0, ExpressionParser.parse("1 ^ 1"));
        // NOT
        assertEquals(1, ExpressionParser.parse("not(0)"));
        assertEquals(0, ExpressionParser.parse("not(1)"));

        assertEquals(1, ExpressionParser.parse("!0"));
        assertEquals(0, ExpressionParser.parse("!1"));
        // NOR
        assertEquals(1, ExpressionParser.parse("nor(0, 0)"));
        assertEquals(0, ExpressionParser.parse("nor(0, 1)"));
        assertEquals(0, ExpressionParser.parse("nor(1, 0)"));
        assertEquals(0, ExpressionParser.parse("nor(1, 1)"));

        assertEquals(1, ExpressionParser.parse("!(0 || 0)"));
        assertEquals(0, ExpressionParser.parse("!(0 || 1)"));
        assertEquals(0, ExpressionParser.parse("!(1 || 0)"));
        assertEquals(0, ExpressionParser.parse("!(1 || 1)"));
        // XNOR
        assertEquals(1, ExpressionParser.parse("xnor(0, 0)"));
        assertEquals(0, ExpressionParser.parse("xnor(0, 1)"));
        assertEquals(0, ExpressionParser.parse("xnor(1, 0)"));
        assertEquals(1, ExpressionParser.parse("xnor(1, 1)"));

        assertEquals(1, ExpressionParser.parse("!(0 ^ 0)"));
        assertEquals(0, ExpressionParser.parse("!(0 ^ 1)"));
        assertEquals(0, ExpressionParser.parse("!(1 ^ 0)"));
        assertEquals(1, ExpressionParser.parse("!(1 ^ 1)"));
        // bool()
        assertEquals(0, ExpressionParser.parse("bool(0.0)"));
        assertEquals(1, ExpressionParser.parse("bool(1.34)"));
        assertEquals(1, ExpressionParser.parse("bool(-12.333 / 2.3)"));
        assertEquals(0, ExpressionParser.parse("bool(1 - 1)"));
    }

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
    void testOtherMultipleCharOperators() {
        assertEquals(1, ExpressionParser.parse("1<= 2.0"));
        assertEquals(1, ExpressionParser.parse("1 <= 2"));
        assertEquals(1, ExpressionParser.parse("1<=2"));
        assertEquals(1, ExpressionParser.parse("1 <=2"));
        assertEquals(1, ExpressionParser.parse("1<=  2"));
        assertEquals(1, ExpressionParser.parse("1 <=    2"));
        assertEquals(0, ExpressionParser.parse("8 <=5.22"));
        assertEquals(0, ExpressionParser.parse("8.111134<=3"));

        assertEquals(1, ExpressionParser.parse("3>= 1"));
        assertEquals(1, ExpressionParser.parse("3>= 1"));
        assertEquals(1, ExpressionParser.parse("3 >= 1"));
        assertEquals(1, ExpressionParser.parse("3  >= 1.18"));
        assertEquals(1, ExpressionParser.parse("3.12  >= 1.18"));
        assertEquals(1, ExpressionParser.parse("1 >=0.0"));
        assertEquals(1, ExpressionParser.parse("1>=-9.11900"));
        assertEquals(1, ExpressionParser.parse("-0.8 >= -9.11900"));

        assertEquals(1, ExpressionParser.parse("3> 1.2"));
        assertEquals(1, ExpressionParser.parse("16/ 2.0 < 999999"));
        assertEquals(1, ExpressionParser.parse("3 < 10"));
        assertEquals(1, ExpressionParser.parse("19> 2.00001"));
        assertEquals(1, ExpressionParser.parse("16 > pow(1, 111)"));
        assertEquals(1, ExpressionParser.parse("18/ 2>1.00"));
        assertEquals(1, ExpressionParser.parse("1>-9.11900"));
        assertEquals(1, ExpressionParser.parse("-0.8 > -9.11900"));

        assertEquals(4, ExpressionParser.parse("2 << 1"));
        assertEquals(4, ExpressionParser.parse("2<< 1"));
        assertEquals(4, ExpressionParser.parse("2<<1"));
        assertEquals(4, ExpressionParser.parse("2<< 1"));
        assertEquals(16, ExpressionParser.parse("2  <<    3"));

        assertEquals(1, ExpressionParser.parse("2 >> 1"));
        assertEquals(4, ExpressionParser.parse("16>>2"));
        assertEquals(4, ExpressionParser.parse("32>> 3"));
        assertEquals(16, ExpressionParser.parse("64  >>    2"));

        assertEquals(0, ExpressionParser.parse("1== 2"));
        assertEquals(0, ExpressionParser.parse("1 == 2"));
        assertEquals(0, ExpressionParser.parse("1==2"));
        assertEquals(0, ExpressionParser.parse("1 ==2"));
        assertEquals(0, ExpressionParser.parse("1==  2"));
        assertEquals(0, ExpressionParser.parse("1 ==    2"));
        assertEquals(0, ExpressionParser.parse("8 ==5.22"));
        assertEquals(0, ExpressionParser.parse("8.111134==3"));

        assertEquals(1, ExpressionParser.parse("1!= 23300"));
        assertEquals(1, ExpressionParser.parse("1 != 2"));
        assertEquals(1, ExpressionParser.parse("1!=2"));
        assertEquals(1, ExpressionParser.parse("1 !=2"));
        assertEquals(1, ExpressionParser.parse("1!=  2.4"));
        assertEquals(1, ExpressionParser.parse("1.44 !=    2"));
        assertEquals(1, ExpressionParser.parse("8 !=5.22"));
        assertEquals(1, ExpressionParser.parse("8.111134!=3"));

        assertEquals(1, ExpressionParser.parse("1&& 23300.0"));
        assertEquals(1, ExpressionParser.parse("1 && 2"));
        assertEquals(0, ExpressionParser.parse("0&&0.00"));
        assertEquals(1, ExpressionParser.parse("1 &&2"));
        assertEquals(1, ExpressionParser.parse("1.0&&  2.4"));
        assertEquals(0, ExpressionParser.parse("0.00 &&    2"));
        assertEquals(1, ExpressionParser.parse("8 &&5.22"));
        assertEquals(1, ExpressionParser.parse("8.111134&&3"));

        assertEquals(1, ExpressionParser.parse("1|| 23300.0"));
        assertEquals(1, ExpressionParser.parse("1 || 2"));
        assertEquals(0, ExpressionParser.parse("0||0.00"));
        assertEquals(1, ExpressionParser.parse("0 ||2"));
        assertEquals(1, ExpressionParser.parse("1.0||  2.4"));
        assertEquals(0, ExpressionParser.parse("0.00 ||    0"));
        assertEquals(1, ExpressionParser.parse("8 ||5.22"));
        assertEquals(1, ExpressionParser.parse("8.111134||3"));
    }

    @Test
    void testOperatorsCombinedWithFunction() {
        assertDoesNotThrow(() -> {
            double result = ExpressionParser.parse("max(-min(1, 3), 5)");
            assertEquals(5, result);
        });

    }
}