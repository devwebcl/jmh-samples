package com.simonj;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class MyBenchmark {

    @Param({"100"}) //1000000
    public int size;

    @Param({"1000", /*"10000", "100000", "1000000",  "10000000", "100000000"*/})
    public int bound;

    private int[] numbers;

    @Setup
    public void setup() {
        numbers = new int[size];
        Random random = new Random(1337);
        for (int i = 0; i < size; i++) {
            numbers[i] = random.nextInt(bound);
        }
        Arrays.sort(numbers);
    }

    @Benchmark
    public void testBranched(Blackhole bh) {
        int uniques = numbers.length > 0 ? 1 : 0;
        for (int i = 0; i < numbers.length - 1; i++) {
            if (numbers[i] != numbers[i + 1]) {
                uniques += 1;
            }
        }
        bh.consume(uniques);
    }

    @Benchmark
    public void testShifted(Blackhole bh) {
        int uniques = numbers.length > 0 ? 1 : 0;
        for (int i = 0; i < numbers.length - 1; i++) {
            uniques += (numbers[i] - numbers[i + 1]) >>> 31;
        }
        bh.consume(uniques);
    }

    public static void main(String[] args) throws RunnerException {
        Options opts = new OptionsBuilder()
                .include(".*")
                .warmupIterations(2) //10
                .measurementIterations(3) //20
                .jvmArgs("-Xms2g", "-Xmx2g")
                .shouldDoGC(true)
                .forks(1)
                .build();

        new Runner(opts).run();
    }
}
