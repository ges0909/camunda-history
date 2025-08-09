package com.valantic.sti.benchmark;

import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)        // ⏱ Messmodus
@OutputTimeUnit(TimeUnit.MICROSECONDS)  // 📏 Zeiteinheit
@State(Scope.Thread)                    // 🧵 Zustand pro Thread
public class ExampleBenchmark {

    List<Integer> testData;

    @Setup(Level.Iteration)             // 🔧 Vor jedem Durchlauf
    public void setup() {
        testData = IntStream.range(0, 1000).boxed().collect(Collectors.toList());
    }

    @TearDown(Level.Iteration)          // 🧹 Nach jedem Durchlauf
    public void teardown() {
        testData = null;
    }

    @Benchmark
    public void benchmarkHeavyCalculation() {
        Collections.sort(new ArrayList<>(testData));
    }
}
