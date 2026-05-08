package br.av2.algorithms.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Odd-Even Transposition Sort: variante paralela clássica do Bubble Sort.
 * Em cada fase par, threads comparam pares (0,1),(2,3),...
 * Em cada fase ímpar, threads comparam pares (1,2),(3,4),...
 */
public class ParallelBubbleSort {

    public static void sort(int[] arr, int threads) throws Exception {
        int n = arr.length;
        if (n <= 1) return;

        CyclicBarrier barrier = new CyclicBarrier(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Future<?>> futures = new ArrayList<>();

        int[] phaseHolder = {0};
        boolean[] swappedHolder = {true};

        for (int t = 0; t < threads; t++) {
            final int threadId = t;
            futures.add(pool.submit(() -> {
                for (int phase = 0; phase < n; phase++) {
                    int start = (phase % 2 == 0) ? threadId * 2 : threadId * 2 + 1;
                    for (int j = start; j < n - 1; j += threads * 2) {
                        if (arr[j] > arr[j + 1]) {
                            int tmp = arr[j];
                            arr[j] = arr[j + 1];
                            arr[j + 1] = tmp;
                        }
                    }
                    try {
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }));
        }

        for (Future<?> f : futures) f.get();
        pool.shutdown();
    }
}
