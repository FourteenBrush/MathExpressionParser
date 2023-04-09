package me.fourteendoggo.mathexpressionparser;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

// TODO: new benchmarks that represent the current state, also mention them in the readme
@State(Scope.Benchmark)
@BenchmarkMode(Mode.All)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class ExpressionParserBenchmark {
    private static final int NUM_EXPRESSIONS = 1000;
    private final int[] firstOperands = new int[NUM_EXPRESSIONS];
    private final int[] secondOperands = new int[NUM_EXPRESSIONS];

    @Setup
    public void setup() {
        Random random = ThreadLocalRandom.current();
        for (int i = 0; i < NUM_EXPRESSIONS; i++) {
            firstOperands[i] = random.nextInt(10000);
            secondOperands[i] = random.nextInt(10000);
        }
    }

    @Benchmark
    @Fork(value = 2, warmups = 2)
    public void parseMultipleExpressions(Blackhole blackhole) {
        for (int i = 0; i < NUM_EXPRESSIONS; i++) {
            double result = ExpressionParser.parse(firstOperands[i] + "*" + secondOperands[i]);

            blackhole.consume(result);
        }
    }
}
