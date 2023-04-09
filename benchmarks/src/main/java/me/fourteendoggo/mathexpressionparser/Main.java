package me.fourteendoggo.mathexpressionparser;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class Main {
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ExpressionParserBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
