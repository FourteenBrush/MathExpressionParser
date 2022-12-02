package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.benchmarks.ExpressionParserBenchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws RunnerException {
        executeNormally();
        //benchmark();
    }

    private static void executeNormally() {
        Scanner in = new Scanner(System.in);

        while (true) {
            System.out.print("Enter an expression or type 'exit' to exit: ");
            String input = in.nextLine();
            if (input.equals("exit")) break;

            long now = System.nanoTime();
            double result;
            try {
                result = ExpressionParser.parse(input);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
                continue;
            }
            long elapsedMicros = (System.nanoTime() - now) / 1000;

            System.out.printf("Result: %s (%d µs)%n", result, elapsedMicros);
        }
    }

    private static void benchmark() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ExpressionParserBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
