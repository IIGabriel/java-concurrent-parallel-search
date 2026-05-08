package br.av2.gui;

import br.av2.benchmark.BenchmarkResult;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ChartPanelWrapper extends JPanel {

    public ChartPanelWrapper(List<BenchmarkResult> results) {
        setLayout(new GridLayout(2, 2, 5, 5));
        add(buildSerialComparisonChart(results));
        add(buildParallelScalingChart(results));
        add(buildThreadsImpactChart(results));
        add(buildDataSizeChart(results));
    }

    private ChartPanel buildSerialComparisonChart(List<BenchmarkResult> results) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String[] serialAlgos = {"BubbleSort", "QuickSort", "MergeSort", "SelectionSort"};

        List<BenchmarkResult> serial = results.stream()
                .filter(r -> r.threads == 1 && Arrays.asList(serialAlgos).contains(r.algorithm))
                .filter(r -> r.dataSize == 10_000)
                .collect(Collectors.toList());

        for (BenchmarkResult r : serial) {
            dataset.addValue(r.average / 1_000_000.0, r.algorithm, r.dataType);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Comparação Serial (10.000 elementos)",
                "Tipo de Dado", "Tempo Médio (ms)",
                dataset, PlotOrientation.VERTICAL, true, true, false);

        styleChart(chart);
        return new ChartPanel(chart);
    }

    private ChartPanel buildParallelScalingChart(List<BenchmarkResult> results) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String[] parallelAlgos = {"ParallelMergeSort", "ParallelQuickSort"};

        for (String algo : parallelAlgos) {
            for (int size : new int[]{10_000, 50_000, 100_000}) {
                OptionalDouble avg = results.stream()
                        .filter(r -> r.algorithm.equals(algo) && r.dataSize == size
                                && r.threads == 4 && r.dataType.equals("Aleatório"))
                        .mapToDouble(r -> r.average / 1_000_000.0)
                        .average();
                if (avg.isPresent()) {
                    dataset.addValue(avg.getAsDouble(), algo, String.valueOf(size));
                }
            }
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Escalabilidade Paralela (4 threads, Aleatório)",
                "Tamanho do Dado", "Tempo Médio (ms)",
                dataset, PlotOrientation.VERTICAL, true, true, false);

        styleChart(chart);
        return new ChartPanel(chart);
    }

    private ChartPanel buildThreadsImpactChart(List<BenchmarkResult> results) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String[] algos = {"ParallelMergeSort", "ParallelQuickSort", "ParallelSelectionSort"};

        for (String algo : algos) {
            for (int threads : new int[]{1, 2, 4, 8}) {
                OptionalDouble avg = results.stream()
                        .filter(r -> r.algorithm.equals(algo) && r.threads == threads
                                && r.dataSize == 50_000 && r.dataType.equals("Aleatório"))
                        .mapToDouble(r -> r.average / 1_000_000.0)
                        .average();
                if (avg.isPresent()) {
                    dataset.addValue(avg.getAsDouble(), algo, threads + " threads");
                }
            }
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Impacto do Número de Threads (50.000 elem., Aleatório)",
                "Threads", "Tempo Médio (ms)",
                dataset, PlotOrientation.VERTICAL, true, true, false);

        styleChart(chart);
        return new ChartPanel(chart);
    }

    private ChartPanel buildDataSizeChart(List<BenchmarkResult> results) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String[] algos = {"MergeSort", "QuickSort", "BubbleSort", "SelectionSort"};

        for (String algo : algos) {
            for (int size : new int[]{1_000, 5_000, 10_000, 50_000, 100_000}) {
                OptionalDouble avg = results.stream()
                        .filter(r -> r.algorithm.equals(algo) && r.dataSize == size
                                && r.dataType.equals("Aleatório"))
                        .mapToDouble(r -> r.average / 1_000_000.0)
                        .average();
                if (avg.isPresent()) {
                    dataset.addValue(avg.getAsDouble(), algo, String.valueOf(size));
                }
            }
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Crescimento por Tamanho de Dado (Serial, Aleatório)",
                "Tamanho do Dado", "Tempo Médio (ms)",
                dataset, PlotOrientation.VERTICAL, true, true, false);

        styleChart(chart);
        return new ChartPanel(chart);
    }

    private void styleChart(JFreeChart chart) {
        chart.setBackgroundPaint(new Color(245, 248, 255));
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 13));
    }
}
