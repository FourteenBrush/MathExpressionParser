package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.benchmarks.ExpressionParserBenchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Scanner;
import java.util.function.DoubleSupplier;

public class Main {

    public static void main(String[] args) throws ClassNotFoundException {
        // initialize function container
        Class.forName("me.fourteendoggo.mathexpressionparser.Tokenizer");
        executeNormally();
        //benchmark();
    }

    private static void executeNormally() {
        Scanner in = new Scanner(System.in);
        boolean firstRun = true;

        while (true) {
            System.out.print("Enter an expression or type 'exit' to exit: ");
            String input = in.nextLine();
            if (input.equals("exit")) break;

            if (firstRun) {
                //parse(() -> eval(input), "Result: %s (%d µs) (eval) (unoptimized)%n");
                parse(() -> ExpressionParser.parse(input), "Result: %s (%d µs) (ExpressionParser) (unoptimized)%n");
                firstRun = false;
            } else {
                //parse(() -> eval(input), "Result: %s (%d µs) (eval) (optimized)%n");
                parse(() -> ExpressionParser.parse(input), "Result: %s (%d µs) (ExpressionParser) (optimized)%n");
            }
        }
    }

    private static void parse(DoubleSupplier parser, String formatString) {
        long now = System.nanoTime();
        double result = 0;
        try {
            result = parser.getAsDouble();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        long elapsedMicros = (System.nanoTime() - now) / 1000;
        System.out.printf(formatString, result, elapsedMicros);
    }

    private static void benchmark() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ExpressionParserBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

    /* testing some stuff */
    public static double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ')
                    nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (; ; ) {
                    if (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (; ; ) {
                    if (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return +parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    if (!eat(')')) throw new RuntimeException("Missing ')'");
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.')
                        nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') {
                    while (ch >= 'a' && ch <= 'z')
                        nextChar();
                    String func = str.substring(startPos, this.pos);
                    if (eat('(')) {
                        x = parseExpression();
                        if (!eat(')')) throw new RuntimeException("Missing ')' after argument to " + func);
                    } else {
                        x = parseFactor();
                    }
                    x = switch (func) {
                        case "sqrt" -> Math.sqrt(x);
                        case "sin" -> Math.sin(Math.toRadians(x));
                        case "cos" -> Math.cos(Math.toRadians(x));
                        case "tan" -> Math.tan(Math.toRadians(x));
                        default -> throw new RuntimeException("Unknown function: " + func);
                    };
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor());

                return x;
            }
        }.parse();
    }
}
