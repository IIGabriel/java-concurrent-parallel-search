package br.av2.benchmark;

import java.util.Arrays;

public class BenchmarkResult {

    public final String algorithm;
    public final int dataSize;
    public final String dataType;
    public final int threads;
    public final long[] samples;
    public final double average;
    public final double stdDev;

    public BenchmarkResult(String algorithm, int dataSize, String dataType, int threads, long[] samples) {
        this.algorithm = algorithm;
        this.dataSize = dataSize;
        this.dataType = dataType;
        this.threads = threads;
        this.samples = samples;
        this.average = computeAverage(samples);
        this.stdDev = computeStdDev(samples, this.average);
    }

    private static double computeAverage(long[] samples) {
        long sum = 0;
        for (long s : samples) sum += s;
        return (double) sum / samples.length;
    }

    private static double computeStdDev(long[] samples, double avg) {
        double variance = 0;
        for (long s : samples) variance += Math.pow(s - avg, 2);
        return Math.sqrt(variance / samples.length);
    }

    public String toCSVRow() {
        StringBuilder sb = new StringBuilder();
        sb.append(algorithm).append(",")
          .append(dataSize).append(",")
          .append(dataType).append(",")
          .append(threads).append(",");
        for (long s : samples) sb.append(s).append(",");
        sb.append(String.format("%.2f", average)).append(",");
        sb.append(String.format("%.2f", stdDev));
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("[%s | tamanho=%d | tipo=%s | threads=%d | média=%.2f ms | σ=%.2f ms]",
                algorithm, dataSize, dataType, threads, average / 1_000_000.0, stdDev / 1_000_000.0);
    }
}
