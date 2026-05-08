package br.av2.algorithms.parallel;

import br.av2.algorithms.serial.MergeSort;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ParallelMergeSort {

    private static final int THRESHOLD = 2048;

    public static void sort(int[] arr, int threads) {
        ForkJoinPool pool = new ForkJoinPool(threads);
        try {
            pool.invoke(new MergeSortTask(arr, 0, arr.length - 1));
        } finally {
            pool.shutdown();
        }
    }

    private static class MergeSortTask extends RecursiveAction {
        private final int[] arr;
        private final int left;
        private final int right;

        MergeSortTask(int[] arr, int left, int right) {
            this.arr = arr;
            this.left = left;
            this.right = right;
        }

        @Override
        protected void compute() {
            if (right - left <= THRESHOLD) {
                MergeSort.sort(arr, left, right);
                return;
            }
            int mid = (left + right) / 2;
            MergeSortTask leftTask = new MergeSortTask(arr, left, mid);
            MergeSortTask rightTask = new MergeSortTask(arr, mid + 1, right);
            invokeAll(leftTask, rightTask);
            MergeSort.merge(arr, left, mid, right);
        }
    }
}
