package br.av2.benchmark;

import java.util.Random;

public class DataGenerator {

    public enum DataType {
        RANDOM("Aleatório"),
        SORTED("Ordenado"),
        REVERSE_SORTED("Inverso"),
        NEARLY_SORTED("Quase Ordenado");

        public final String label;
        DataType(String label) { this.label = label; }
    }

    private static final Random rng = new Random(42);

    public static int[] generate(int size, DataType type) {
        int[] arr = new int[size];
        switch (type) {
            case RANDOM:
                for (int i = 0; i < size; i++) arr[i] = rng.nextInt(size * 10);
                break;
            case SORTED:
                for (int i = 0; i < size; i++) arr[i] = i;
                break;
            case REVERSE_SORTED:
                for (int i = 0; i < size; i++) arr[i] = size - i;
                break;
            case NEARLY_SORTED:
                for (int i = 0; i < size; i++) arr[i] = i;
                int swaps = Math.max(1, size / 20);
                for (int i = 0; i < swaps; i++) {
                    int a = rng.nextInt(size);
                    int b = rng.nextInt(size);
                    int tmp = arr[a];
                    arr[a] = arr[b];
                    arr[b] = tmp;
                }
                break;
        }
        return arr;
    }

    public static int[] copy(int[] original) {
        int[] copy = new int[original.length];
        System.arraycopy(original, 0, copy, 0, original.length);
        return copy;
    }
}
