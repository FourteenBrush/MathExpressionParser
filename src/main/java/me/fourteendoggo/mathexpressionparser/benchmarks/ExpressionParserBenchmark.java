package me.fourteendoggo.mathexpressionparser.benchmarks;

import me.fourteendoggo.mathexpressionparser.ExpressionParser;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class ExpressionParserBenchmark {
    private ThreadLocalRandom random;

    @Setup
    public void setup() {
        random = ThreadLocalRandom.current();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Fork(value = 2, warmups = 2)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void parseSingleExpression(Blackhole blackhole) {
        double result = ExpressionParser.parse("3*5");

        blackhole.consume(result);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Fork(value = 2, warmups = 2)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void parseMultipleExpressions(Blackhole blackhole) {
        for (int i = 0; i < 100; i++) {
            int firstOperand = random.nextInt(0, 1000);
            int secondOperand = random.nextInt(0, 1000);

            double result = ExpressionParser.parse(firstOperand + "*" + secondOperand);

            blackhole.consume(result);
        }
    }
}
