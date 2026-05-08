package br.av2;

import br.av2.benchmark.BenchmarkRunner;
import br.av2.benchmark.BenchmarkResult;
import br.av2.csv.CSVWriter;
import br.av2.gui.MainFrame;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        boolean headless = args.length > 0 && args[0].equals("--cli");

        if (headless) {
            runCLI();
        } else {
            SwingUtilities.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {}
                new MainFrame().setVisible(true);
            });
        }
    }

    private static void runCLI() throws Exception {
        System.out.println("=== Benchmark CLI - Algoritmos de Ordenação ===\n");
        BenchmarkRunner runner = new BenchmarkRunner(System.out::println);
        List<BenchmarkResult> results = runner.runAll();

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String csvPath = "benchmark_" + timestamp + ".csv";
        CSVWriter.write(results, csvPath);

        System.out.println("\n=== RESUMO ===");
        System.out.printf("Total de execuções: %d%n", results.size());
        System.out.println("CSV salvo em: " + csvPath);
    }
}
