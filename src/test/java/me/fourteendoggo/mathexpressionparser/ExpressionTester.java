package me.fourteendoggo.mathexpressionparser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ExpressionTester {

    public static void main(String[] args) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("src", "test", "tests.txt"));
             BufferedWriter writer = Files.newBufferedWriter(Paths.get("src", "test", "results.txt"))) {
            String expression;
            int errors = 0;
            while ((expression = reader.readLine()) != null) {
                if (expression.isBlank() || expression.startsWith("#")) continue;

                String expectedResult = reader.readLine();
                double result;
                try {
                    result = ExpressionParser.parse(expression);
                } catch (Throwable t) {
                    errors++;
                    writer.write("Expression %s threw an %s, error message: %s%n".formatted(expression, t.getClass().getSimpleName(), t.getMessage()));
                    continue;
                }

                double expected = new Expression(expectedResult.toCharArray()).parse();
                if (result != expected) {
                    errors++;
                    writer.write("Expression %s returned %.8f instead of %.8f%n".formatted(expression, result, expected));
                }
            }
            if (errors == 0) {
                System.out.println("✅ All tests passed");
            } else {
                System.out.println("❌ " + errors + " tests failed, results got written to results.txt");
            }
        }
    }
}
