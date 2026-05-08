package br.av2.gui;

import br.av2.benchmark.BenchmarkResult;
import br.av2.benchmark.BenchmarkRunner;
import br.av2.csv.CSVWriter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainFrame extends JFrame {

    private JTextArea logArea;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JButton runButton;
    private JTabbedPane tabbedPane;
    private JProgressBar progressBar;
    private List<BenchmarkResult> lastResults;

    public MainFrame() {
        super("Análise de Desempenho - Algoritmos de Ordenação");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout(5, 5));
        getRootPane().setBorder(new EmptyBorder(8, 8, 8, 8));

        // Painel superior
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.setBackground(new Color(30, 50, 80));

        JLabel title = new JLabel("  Benchmark de Algoritmos de Ordenação - Serial vs Paralelo");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 16));

        runButton = new JButton("Executar Benchmark");
        runButton.setBackground(new Color(0, 180, 100));
        runButton.setForeground(Color.WHITE);
        runButton.setFont(new Font("Arial", Font.BOLD, 13));
        runButton.setFocusPainted(false);
        runButton.addActionListener(e -> startBenchmark());

        JButton exportButton = new JButton("Exportar CSV");
        exportButton.setBackground(new Color(0, 120, 200));
        exportButton.setForeground(Color.WHITE);
        exportButton.setFont(new Font("Arial", Font.BOLD, 13));
        exportButton.setFocusPainted(false);
        exportButton.addActionListener(e -> exportCSV());

        topPanel.add(title);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(runButton);
        topPanel.add(exportButton);
        add(topPanel, BorderLayout.NORTH);

        // Abas centrais
        tabbedPane = new JTabbedPane();

        // Aba: Log
        logArea = new JTextArea();
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setBackground(new Color(20, 20, 30));
        logArea.setForeground(new Color(0, 230, 100));
        logArea.setEditable(false);
        tabbedPane.addTab("Log de Execução", new JScrollPane(logArea));

        // Aba: Tabela
        String[] columns = {"Algoritmo", "Tamanho", "Tipo de Dado", "Threads",
                "S1(ms)", "S2(ms)", "S3(ms)", "S4(ms)", "S5(ms)", "Média(ms)", "σ(ms)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        resultTable = new JTable(tableModel);
        resultTable.setAutoCreateRowSorter(true);
        resultTable.setRowHeight(22);
        resultTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tabbedPane.addTab("Resultados", new JScrollPane(resultTable));

        // Aba: Gráficos (serão adicionados após benchmark)
        tabbedPane.addTab("Gráficos", new JPanel());

        add(tabbedPane, BorderLayout.CENTER);

        // Barra de progresso
        progressBar = new JProgressBar();
        progressBar.setString("Pronto");
        progressBar.setStringPainted(true);
        progressBar.setIndeterminate(false);
        add(progressBar, BorderLayout.SOUTH);
    }

    private void startBenchmark() {
        runButton.setEnabled(false);
        progressBar.setIndeterminate(true);
        progressBar.setString("Executando benchmark...");
        logArea.setText("");
        tableModel.setRowCount(0);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                BenchmarkRunner runner = new BenchmarkRunner(msg ->
                        SwingUtilities.invokeLater(() -> {
                            logArea.append(msg + "\n");
                            logArea.setCaretPosition(logArea.getDocument().getLength());
                        })
                );
                lastResults = runner.runAll();

                SwingUtilities.invokeLater(() -> {
                    populateTable(lastResults);
                    buildCharts(lastResults);
                    progressBar.setIndeterminate(false);
                    progressBar.setString("Concluído! " + lastResults.size() + " resultados.");
                    runButton.setEnabled(true);
                    tabbedPane.setSelectedIndex(1);
                    JOptionPane.showMessageDialog(this, "Benchmark concluído!\n" + lastResults.size() + " execuções registradas.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    logArea.append("ERRO: " + ex.getMessage() + "\n");
                    progressBar.setIndeterminate(false);
                    progressBar.setString("Erro durante execução.");
                    runButton.setEnabled(true);
                });
            }
        });
        executor.shutdown();
    }

    private void populateTable(List<BenchmarkResult> results) {
        tableModel.setRowCount(0);
        for (BenchmarkResult r : results) {
            Object[] row = new Object[11];
            row[0] = r.algorithm;
            row[1] = r.dataSize;
            row[2] = r.dataType;
            row[3] = r.threads;
            for (int i = 0; i < 5; i++) {
                row[4 + i] = String.format("%.3f", r.samples[i] / 1_000_000.0);
            }
            row[9]  = String.format("%.3f", r.average / 1_000_000.0);
            row[10] = String.format("%.3f", r.stdDev  / 1_000_000.0);
            tableModel.addRow(row);
        }
    }

    private void buildCharts(List<BenchmarkResult> results) {
        ChartPanelWrapper charts = new ChartPanelWrapper(results);
        tabbedPane.setComponentAt(2, charts);
        tabbedPane.setTitleAt(2, "Gráficos");
    }

    private void exportCSV() {
        if (lastResults == null || lastResults.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Execute o benchmark antes de exportar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        chooser.setSelectedFile(new File("benchmark_" + timestamp + ".csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                CSVWriter.write(lastResults, chooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, "CSV exportado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao exportar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
