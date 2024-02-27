package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class ExpressionParserTest {

    @ParameterizedTest
    @CsvFileSource(resources = "/positive-input.csv")
    void testPositiveTestCases(String expression, String expectedResult) {
        double expected = ExpressionParser.parse(expectedResult);
        double result = assertDoesNotThrow(() -> ExpressionParser.parse(expression));
        // delta 0 to make 0.0 == -0.0 pass, junit uses Double.doubleToLongBits for comparison
        assertThat(result).withFailMessage("Got %f instead of %f", result, expected).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/negative-input.csv")
    void testNegativeTestCases(String expression) {
        assertThatCode(() -> ExpressionParser.parse(expression)).isInstanceOf(SyntaxException.class);
    }

    @Test
    void testThrowingExpressions() {
        assertThatThrownBy(() -> ExpressionParser.parse(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> ExpressionParser.parse("")).isInstanceOf(SyntaxException.class);
    }

    @Test
    void testBooleanTruthTables() {
        // AND
        assertThat(ExpressionParser.parse("and(0, 0)")).isZero();
        assertThat(ExpressionParser.parse("and(0, 1)")).isZero();
        assertThat(ExpressionParser.parse("and(1, 0)")).isZero();
        assertThat(ExpressionParser.parse("and(1, 1)")).isOne();

        assertThat(ExpressionParser.parse("0 && 0")).isZero();
        assertThat(ExpressionParser.parse("0 && 1")).isZero();
        assertThat(ExpressionParser.parse("1 && 0")).isZero();
        assertThat(ExpressionParser.parse("1 && 1")).isOne();
        // NAND
        assertThat(ExpressionParser.parse("nand(0, 0)")).isOne();
        assertThat(ExpressionParser.parse("nand(0, 1)")).isOne();
        assertThat(ExpressionParser.parse("nand(1, 0)")).isOne();
        assertThat(ExpressionParser.parse("nand(1, 1)")).isZero();

        assertThat(ExpressionParser.parse("!(0 && 0)")).isOne();
        assertThat(ExpressionParser.parse("!(0 && 0)")).isOne();
        assertThat(ExpressionParser.parse("!(0 && 0)")).isOne();
        assertThat(ExpressionParser.parse("!(0 && 0)")).isOne();

        assertThat(ExpressionParser.parse("!(0 && 0)")).isOne();
        assertThat(ExpressionParser.parse("!(0 && 1)")).isOne();
        assertThat(ExpressionParser.parse("!(1 && 0)")).isOne();
        assertThat(ExpressionParser.parse("!(1 && 1)")).isZero();
        // OR
        assertThat(ExpressionParser.parse("0 || 0")).isZero();
        assertThat(ExpressionParser.parse("0 || 1")).isOne();
        assertThat(ExpressionParser.parse("1 || 0")).isOne();
        assertThat(ExpressionParser.parse("1 || 1")).isOne();
        // XOR
        assertThat(ExpressionParser.parse("xor(0, 0)")).isZero();
        assertThat(ExpressionParser.parse("xor(0, 1)")).isOne();
        assertThat(ExpressionParser.parse("xor(1, 0)")).isOne();
        assertThat(ExpressionParser.parse("xor(1, 1)")).isZero();

        assertThat(ExpressionParser.parse("0 ^ 0")).isZero();
        assertThat(ExpressionParser.parse("0 ^ 1")).isOne();
        assertThat(ExpressionParser.parse("1 ^ 0")).isOne();
        assertThat(ExpressionParser.parse("1 ^ 1")).isZero();
        // NOT
        assertThat(ExpressionParser.parse("not(0)")).isOne();
        assertThat(ExpressionParser.parse("not(1)")).isZero();

        assertThat(ExpressionParser.parse("!0")).isOne();
        assertThat(ExpressionParser.parse("!1")).isZero();
        // NOR
        assertThat(ExpressionParser.parse("nor(0, 0)")).isOne();
        assertThat(ExpressionParser.parse("nor(0, 1)")).isZero();
        assertThat(ExpressionParser.parse("nor(1, 0)")).isZero();
        assertThat(ExpressionParser.parse("nor(1, 1)")).isZero();

        assertThat(ExpressionParser.parse("!(0 || 0)")).isOne();
        assertThat(ExpressionParser.parse("!(0 || 1)")).isZero();
        assertThat(ExpressionParser.parse("!(1 || 0)")).isZero();
        assertThat(ExpressionParser.parse("!(1 || 1)")).isZero();
        // XNOR
        assertThat(ExpressionParser.parse("xnor(0, 0)")).isOne();
        assertThat(ExpressionParser.parse("xnor(0, 1)")).isZero();
        assertThat(ExpressionParser.parse("xnor(1, 0)")).isZero();
        assertThat(ExpressionParser.parse("xnor(1, 1)")).isOne();

        assertThat(ExpressionParser.parse("!(0 ^ 0)")).isOne();
        assertThat(ExpressionParser.parse("!(0 ^ 1)")).isZero();
        assertThat(ExpressionParser.parse("!(1 ^ 0)")).isZero();
        assertThat(ExpressionParser.parse("!(1 ^ 1)")).isOne();
        // bool()
        assertThat(ExpressionParser.parse("bool(0.0)")).isZero();
        assertThat(ExpressionParser.parse("bool(1.34)")).isOne();
        assertThat(ExpressionParser.parse("bool(-12.333 / 2.3)")).isOne();
        assertThat(ExpressionParser.parse("bool(1 - 1)")).isZero();
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
        assertThat(ExpressionParser.parse("1<= 2.0")).isOne();
        assertThat(ExpressionParser.parse("1 <= 2")).isOne();
        assertThat(ExpressionParser.parse("1<=2")).isOne();
        assertThat(ExpressionParser.parse("1 <=2")).isOne();
        assertThat(ExpressionParser.parse("1<=  2")).isOne();
        assertThat(ExpressionParser.parse("1 <=    2")).isOne();
        assertThat(ExpressionParser.parse("8 <=5.22")).isZero();
        assertThat(ExpressionParser.parse("8.111134<=3")).isZero();

        assertThat(ExpressionParser.parse("3>= 1")).isOne();
        assertThat(ExpressionParser.parse("3>= 1")).isOne();
        assertThat(ExpressionParser.parse("3 >= 1")).isOne();
        assertThat(ExpressionParser.parse("3  >= 1.18")).isOne();
        assertThat(ExpressionParser.parse("3.12  >= 1.18")).isOne();
        assertThat(ExpressionParser.parse("1 >=0.0")).isOne();
        assertThat(ExpressionParser.parse("1>=-9.11900")).isOne();
        assertThat(ExpressionParser.parse("-0.8 >= -9.11900")).isOne();

        assertThat(ExpressionParser.parse("3> 1.2")).isOne();
        assertThat(ExpressionParser.parse("16/ 2.0 < 999999")).isOne();
        assertThat(ExpressionParser.parse("3 < 10")).isOne();
        assertThat(ExpressionParser.parse("19> 2.00001")).isOne();
        assertThat(ExpressionParser.parse("16 > pow(1, 111)")).isOne();
        assertThat(ExpressionParser.parse("18/ 2>1.00")).isOne();
        assertThat(ExpressionParser.parse("1>-9.11900")).isOne();
        assertThat(ExpressionParser.parse("-0.8 > -9.11900")).isOne();

        assertThat(ExpressionParser.parse("2 << 1")).isEqualTo(4);
        assertThat(ExpressionParser.parse("2<< 1")).isEqualTo(4);
        assertThat(ExpressionParser.parse("2<<1")).isEqualTo(4);
        assertThat(ExpressionParser.parse("2<< 1")).isEqualTo(4);
        assertThat(ExpressionParser.parse("2  <<    3")).isEqualTo(16);

        assertThat(ExpressionParser.parse("2 >> 1")).isEqualTo(1);
        assertThat(ExpressionParser.parse("16>>2")).isEqualTo(4);
        assertThat(ExpressionParser.parse("32>> 3")).isEqualTo(4);
        assertThat(ExpressionParser.parse("64  >>    2")).isEqualTo(16);

        assertThat(ExpressionParser.parse("1== 2")).isZero();
        assertThat(ExpressionParser.parse("1 == 2")).isZero();
        assertThat(ExpressionParser.parse("1==2")).isZero();
        assertThat(ExpressionParser.parse("1 ==2")).isZero();
        assertThat(ExpressionParser.parse("1==  2")).isZero();
        assertThat(ExpressionParser.parse("1 ==    2")).isZero();
        assertThat(ExpressionParser.parse("8 ==5.22")).isZero();
        assertThat(ExpressionParser.parse("8.111134==3")).isZero();

        assertThat(ExpressionParser.parse("1!= 23300")).isOne();
        assertThat(ExpressionParser.parse("1 != 2")).isOne();
        assertThat(ExpressionParser.parse("1!=2")).isOne();
        assertThat(ExpressionParser.parse("1 !=2")).isOne();
        assertThat(ExpressionParser.parse("1!=  2.4")).isOne();
        assertThat(ExpressionParser.parse("1.44 !=    2")).isOne();
        assertThat(ExpressionParser.parse("8 !=5.22")).isOne();
        assertThat(ExpressionParser.parse("8.111134!=3")).isOne();

        assertThat(ExpressionParser.parse("1&& 23300.0")).isOne();
        assertThat(ExpressionParser.parse("1 && 2")).isOne();
        assertThat(ExpressionParser.parse("0&&0.00")).isZero();
        assertThat(ExpressionParser.parse("1 &&2")).isOne();
        assertThat(ExpressionParser.parse("1.0&&  2.4")).isOne();
        assertThat(ExpressionParser.parse("0.00 &&    2")).isZero();
        assertThat(ExpressionParser.parse("8 &&5.22")).isOne();
        assertThat(ExpressionParser.parse("8.111134&&3")).isOne();

        assertThat(ExpressionParser.parse("1|| 23300.0")).isOne();
        assertThat(ExpressionParser.parse("1 || 2")).isOne();
        assertThat(ExpressionParser.parse("0||0.00")).isZero();
        assertThat(ExpressionParser.parse("0 ||2")).isOne();
        assertThat(ExpressionParser.parse("1.0||  2.4")).isOne();
        assertThat(ExpressionParser.parse("0.00 ||    0")).isZero();
        assertThat(ExpressionParser.parse("8 ||5.22")).isOne();
        assertThat(ExpressionParser.parse("8.111134||3")).isOne();
    }

    @Test
    void testOperatorsCombinedWithFunction() {
        assertThatCode(() -> {
            double res = ExpressionParser.parse("max(-min(1, 3), 5)");
            assertThat(res).isEqualTo(5);
        }).doesNotThrowAnyException();
    }
}