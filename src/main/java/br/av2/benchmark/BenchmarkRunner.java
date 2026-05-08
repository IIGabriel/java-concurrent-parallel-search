package br.av2.benchmark;

import br.av2.algorithms.parallel.*;
import br.av2.algorithms.serial.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BenchmarkRunner {

    public static final int[] DATA_SIZES = {1_000, 5_000, 10_000, 50_000, 100_000};
    public static final int[] THREAD_COUNTS = {1, 2, 4, 8};
    public static final int SAMPLES = 5;

    // BubbleSort e SelectionSort O(n²) são impraticáveis acima deste limite
    private static final int O2_MAX_SIZE = 10_000;

    private final Consumer<String> logger;

    public BenchmarkRunner(Consumer<String> logger) {
        this.logger = logger;
    }

    public List<BenchmarkResult> runAll() {
        List<BenchmarkResult> results = new ArrayList<>();

        for (DataGenerator.DataType dataType : DataGenerator.DataType.values()) {
            for (int size : DATA_SIZES) {
                int[] baseData = DataGenerator.generate(size, dataType);

                // --- Serial ---
                if (size <= O2_MAX_SIZE) {
                    results.add(runSerial("BubbleSort",    size, dataType, baseData, arr -> BubbleSort.sort(arr)));
                    results.add(runSerial("SelectionSort", size, dataType, baseData, arr -> SelectionSort.sort(arr)));
                } else {
                    logger.accept("[BubbleSort | tamanho=" + size + " | IGNORADO (O(n²) impraticável)]");
                    logger.accept("[SelectionSort | tamanho=" + size + " | IGNORADO (O(n²) impraticável)]");
                }
                results.add(runSerial("QuickSort",     size, dataType, baseData, arr -> QuickSort.sort(arr)));
                results.add(runSerial("MergeSort",     size, dataType, baseData, arr -> MergeSort.sort(arr)));

                // --- Paralelo ---
                for (int threads : THREAD_COUNTS) {
                    results.add(runParallel("ParallelMergeSort",    size, dataType, threads, baseData,
                            (arr, t) -> ParallelMergeSort.sort(arr, t)));
                    results.add(runParallel("ParallelQuickSort",    size, dataType, threads, baseData,
                            (arr, t) -> ParallelQuickSort.sort(arr, t)));
                    if (size <= O2_MAX_SIZE) {
                        results.add(runParallel("ParallelBubbleSort",   size, dataType, threads, baseData,
                                (arr, t) -> ParallelBubbleSort.sort(arr, t)));
                        results.add(runParallel("ParallelSelectionSort",size, dataType, threads, baseData,
                                (arr, t) -> ParallelSelectionSort.sort(arr, t)));
                    }
                }
            }
        }
        return results;
    }

    private BenchmarkResult runSerial(String name, int size, DataGenerator.DataType dataType,
                                       int[] baseData, ThrowingConsumer<int[]> sorter) {
        long[] times = new long[SAMPLES];
        for (int i = 0; i < SAMPLES; i++) {
            int[] arr = DataGenerator.copy(baseData);
            long start = System.nanoTime();
            try { sorter.accept(arr); } catch (Exception e) { /* não ocorre */ }
            times[i] = System.nanoTime() - start;
        }
        BenchmarkResult result = new BenchmarkResult(name, size, dataType.label, 1, times);
        logger.accept(result.toString());
        return result;
    }

    private BenchmarkResult runParallel(String name, int size, DataGenerator.DataType dataType,
                                         int threads, int[] baseData, ThrowingBiConsumer<int[], Integer> sorter) {
        long[] times = new long[SAMPLES];
        for (int i = 0; i < SAMPLES; i++) {
            int[] arr = DataGenerator.copy(baseData);
            long start = System.nanoTime();
            try { sorter.accept(arr, threads); } catch (Exception e) {
                logger.accept("Erro em " + name + ": " + e.getMessage());
            }
            times[i] = System.nanoTime() - start;
        }
        BenchmarkResult result = new BenchmarkResult(name, size, dataType.label, threads, times);
        logger.accept(result.toString());
        return result;
    }

    @FunctionalInterface
    public interface ThrowingConsumer<T> {
        void accept(T t) throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingBiConsumer<T, U> {
        void accept(T t, U u) throws Exception;
    }
}
