package br.av2.algorithms.parallel;

import br.av2.algorithms.serial.QuickSort;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ParallelQuickSort {

    private static final int THRESHOLD = 2048;

    public static void sort(int[] arr, int threads) {
        ForkJoinPool pool = new ForkJoinPool(threads);
        try {
            pool.invoke(new QuickSortTask(arr, 0, arr.length - 1));
        } finally {
            pool.shutdown();
        }
    }

    private static class QuickSortTask extends RecursiveAction {
        private final int[] arr;
        private final int low;
        private final int high;

        QuickSortTask(int[] arr, int low, int high) {
            this.arr = arr;
            this.low = low;
            this.high = high;
        }

        @Override
        protected void compute() {
            if (high - low <= THRESHOLD) {
                // Usa versão iterativa serial (evita StackOverflow na recursão)
                QuickSort.sort(arr, low, high);
                return;
            }
            int pi = QuickSort.partition(arr, low, high);
            QuickSortTask leftTask  = new QuickSortTask(arr, low, pi - 1);
            QuickSortTask rightTask = new QuickSortTask(arr, pi + 1, high);
            invokeAll(leftTask, rightTask);
        }
    }
}
