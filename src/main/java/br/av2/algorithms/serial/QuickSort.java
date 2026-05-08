package br.av2.algorithms.serial;

import java.util.ArrayDeque;
import java.util.Deque;

public class QuickSort {

    public static void sort(int[] arr) {
        sort(arr, 0, arr.length - 1);
    }

    // Versão iterativa com pivot mediana-de-três para evitar StackOverflow no pior caso
    public static void sort(int[] arr, int low, int high) {
        if (low >= high) return;
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{low, high});
        while (!stack.isEmpty()) {
            int[] bounds = stack.pop();
            int l = bounds[0], h = bounds[1];
            if (l >= h) continue;
            int pi = partition(arr, l, h);
            if (pi - 1 > l) stack.push(new int[]{l, pi - 1});
            if (pi + 1 < h) stack.push(new int[]{pi + 1, h});
        }
    }

    public static int partition(int[] arr, int low, int high) {
        // Mediana-de-três: evita pior caso em arrays já ordenados
        int mid = low + (high - low) / 2;
        if (arr[low] > arr[mid])  swap(arr, low, mid);
        if (arr[low] > arr[high]) swap(arr, low, high);
        if (arr[mid] > arr[high]) swap(arr, mid, high);
        swap(arr, mid, high); // coloca o pivot (mediana) no final

        int pivot = arr[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (arr[j] <= pivot) swap(arr, ++i, j);
        }
        swap(arr, i + 1, high);
        return i + 1;
    }

    private static void swap(int[] arr, int a, int b) {
        int tmp = arr[a]; arr[a] = arr[b]; arr[b] = tmp;
    }
}
