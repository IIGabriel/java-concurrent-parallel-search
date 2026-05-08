package br.av2.csv;

import br.av2.benchmark.BenchmarkResult;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class CSVWriter {

    private static final String HEADER =
            "Algoritmo,TamanhoDados,TipoDados,Threads,Amostra1(ns),Amostra2(ns),Amostra3(ns),Amostra4(ns),Amostra5(ns),Media(ns),DesvioPadrao(ns)";

    public static void write(List<BenchmarkResult> results, String filePath) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.println(HEADER);
            for (BenchmarkResult r : results) {
                pw.println(r.toCSVRow());
            }
        }
        System.out.println("CSV gerado em: " + filePath);
    }
}
