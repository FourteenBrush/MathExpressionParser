package me.fourteendoggo.mathexpressionparser;

import java.io.*;

public class ExpressionTester {

    public static void main(String[] args) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/test/tests.txt"));
             BufferedWriter writer = new BufferedWriter(new FileWriter("src/test/results.txt"))) {
            String expression, expectedResult;
            int errors = 0;
            while ((expression = reader.readLine()) != null) {
                if (expression.isEmpty() || expression.startsWith("#")) continue;
                expectedResult = reader.readLine();
                double result;

                try {
                    result = ExpressionParser.parse(expression);
                } catch (Throwable e) {
                    errors++;
                    writer.write(format("Expression '%s' threw an %s, error message: %s\n",
                            expression, e.getClass().getSimpleName(), e.getMessage()));
                    continue;
                }

                // for some reason I can't longer rely on new Tokenizer(expectedResult.toCharArray()).readDouble()
                double expected = new Expression(expectedResult.toCharArray()).parse();
                if (result != expected) {
                    errors++;
                    writer.write(format("Expression '%s' returned %.10f, expected %.10f\n",
                            expression, result, expected));
                }
            }

            if (errors == 0) {
                System.out.println("All tests passed");
            } else {
                System.out.println(errors + " tests failed, results got written to results.txt");
            }
        }
    }

    static String format(String input, Object... args) {
        return input.formatted(args);
    }
}
