package br.av2.algorithms.parallel;

import br.av2.algorithms.serial.MergeSort;
import br.av2.algorithms.serial.SelectionSort;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Estratégia: divide o array em segmentos, cada thread ordena seu segmento
 * com Selection Sort serial, depois faz merge sequencial dos segmentos ordenados.
 */
public class ParallelSelectionSort {

    public static void sort(int[] arr, int threads) throws Exception {
        int n = arr.length;
        if (n <= 1) return;

        int segmentSize = (int) Math.ceil((double) n / threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int t = 0; t < threads; t++) {
            final int start = t * segmentSize;
            final int end = Math.min(start + segmentSize, n);
            if (start >= n) break;
            tasks.add(() -> {
                int[] segment = new int[end - start];
                System.arraycopy(arr, start, segment, 0, end - start);
                SelectionSort.sort(segment);
                System.arraycopy(segment, 0, arr, start, end - start);
                return null;
            });
        }

        List<Future<Void>> futures = pool.invokeAll(tasks);
        for (Future<Void> f : futures) f.get();
        pool.shutdown();

        // Merge sequencial dos segmentos ordenados
        mergeSegments(arr, segmentSize, threads);
    }

    private static void mergeSegments(int[] arr, int segmentSize, int threads) {
        int n = arr.length;
        int size = segmentSize;
        while (size < n) {
            for (int left = 0; left < n; left += 2 * size) {
                int mid = Math.min(left + size - 1, n - 1);
                int right = Math.min(left + 2 * size - 1, n - 1);
                if (mid < right) {
                    MergeSort.merge(arr, left, mid, right);
                }
            }
            size *= 2;
        }
    }
}
