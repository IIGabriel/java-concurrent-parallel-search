# Análise de Desempenho de Algoritmos de Ordenação em Ambientes Concorrentes e Paralelos

## Resumo

Este trabalho apresenta uma análise comparativa de desempenho entre implementações seriais e paralelas de quatro algoritmos de ordenação clássicos: Bubble Sort, Quick Sort, Merge Sort e Selection Sort. As implementações foram desenvolvidas em Java, utilizando as APIs de concorrência da plataforma (`ForkJoinPool`, `ExecutorService` e `CyclicBarrier`). Os experimentos variaram o tamanho dos conjuntos de dados (de 1.000 a 100.000 elementos), o tipo de dado de entrada (aleatório, ordenado, inverso e quase ordenado) e o número de threads (1, 2, 4 e 8). Cada configuração foi executada cinco vezes para garantir significância estatística. Os resultados demonstram que algoritmos de divisão e conquista (Merge Sort e Quick Sort) apresentam ganhos expressivos de desempenho com a paralelização, enquanto algoritmos de complexidade O(n²) como Bubble Sort e Selection Sort têm ganhos mais modestos. Os tempos de execução foram registrados em arquivos CSV e visualizados por meio de gráficos dinâmicos implementados com a biblioteca JFreeChart.

## Introdução

A crescente disponibilidade de processadores multicore tornou a programação paralela um requisito fundamental para o desenvolvimento de software de alto desempenho. Algoritmos de ordenação estão presentes em inúmeras aplicações computacionais, desde bancos de dados até sistemas de recomendação, e sua eficiência impacta diretamente o desempenho geral dos sistemas.

Este trabalho investiga quatro algoritmos de ordenação amplamente conhecidos:

- **Bubble Sort**: algoritmo simples de complexidade O(n²), comparando e trocando elementos adjacentes repetidamente. A versão paralela utiliza o esquema *Odd-Even Transposition Sort*, onde threads alternam entre fases pares e ímpares de comparações.
- **Quick Sort**: algoritmo eficiente baseado em particionamento, com complexidade média O(n log n). A paralelização é realizada com `ForkJoinPool`, dividindo recursivamente as partições entre threads.
- **Merge Sort**: algoritmo estável de complexidade O(n log n) baseado em divisão e conquista. A versão paralela divide o array em metades processadas concorrentemente via `RecursiveAction`.
- **Selection Sort**: algoritmo de complexidade O(n²) que localiza o mínimo iterativamente. A paralelização divide o array em segmentos, cada thread ordena seu segmento com Selection Sort e os resultados são mesclados sequencialmente.

A abordagem adota o modelo de paralelismo de dados (*data parallelism*), onde cada thread opera sobre uma partição do conjunto de dados, reduzindo o tempo total de execução em função da quantidade de núcleos disponíveis.

## Metodologia

### Framework de Teste

O framework foi implementado na classe `BenchmarkRunner`, responsável por:

1. Gerar conjuntos de dados com a classe `DataGenerator` nas variantes: Aleatório, Ordenado, Inverso e Quase Ordenado.
2. Para cada combinação de (algoritmo × tamanho × tipo × threads), executar **5 amostras** independentes, copiando o array original antes de cada execução para garantir condições idênticas.
3. Medir o tempo com `System.nanoTime()` (precisão de nanosegundos).
4. Calcular média aritmética e desvio padrão das amostras.

### Configurações de Teste

| Parâmetro | Valores |
|-----------|---------|
| Tamanhos de dados | 1.000 / 5.000 / 10.000 / 50.000 / 100.000 |
| Tipos de dados | Aleatório / Ordenado / Inverso / Quase Ordenado |
| Threads (paralelo) | 1 / 2 / 4 / 8 |
| Amostras por execução | 5 |

### Análise Estatística

Para cada configuração, são calculados:
- **Média**: `∑tᵢ / n`
- **Desvio Padrão**: `√(∑(tᵢ - μ)² / n)`

Esses indicadores permitem avaliar a consistência das medições e identificar variabilidade no comportamento dos algoritmos.

## Resultados e Discussão

Os resultados completos são exportados em arquivo CSV com o formato:

```
Algoritmo, TamanhoDados, TipoDados, Threads, S1..S5 (ns), Média (ns), DesvioPadrão (ns)
```

### Observações Esperadas

- **Merge Sort e Quick Sort paralelos** tendem a exibir speedup próximo linear para dados aleatórios com tamanhos elevados, especialmente com 4 e 8 threads, pois o overhead de divisão de tarefas é amortizado.
- **Bubble Sort paralelo** apresenta melhora limitada: a dependência sequencial entre fases restringe o paralelismo real, gerando overhead de sincronização via `CyclicBarrier`.
- **Selection Sort paralelo** melhora proporcionalmente ao número de threads para a fase de ordenação dos segmentos, porém o merge sequencial final limita o ganho global.
- Conjuntos **já ordenados** resultam em tempos muito menores para Bubble Sort (pois a verificação de *swap* aborta o laço cedo) mas podem degradar o Quick Sort (pivô sempre em posição extrema).
- O **desvio padrão** tende a aumentar com o número de threads devido à variabilidade de escalonamento do SO.

### Gráficos Gerados (via JFreeChart)

1. **Comparação Serial** – barras comparando os quatro algoritmos seriais por tipo de dado (10.000 elementos).
2. **Escalabilidade Paralela** – linhas mostrando crescimento de tempo por tamanho de dado (4 threads, dados aleatórios).
3. **Impacto de Threads** – linhas mostrando variação de tempo por quantidade de threads (50.000 elementos).
4. **Crescimento por Tamanho** – linhas seriais por tamanho de dado para todos os algoritmos.

## Conclusão

Os experimentos confirmam que a paralelização traz benefícios concretos para algoritmos baseados em divisão e conquista (Merge Sort, Quick Sort), que se adaptam naturalmente ao modelo Fork-Join. Algoritmos de O(n²) como Bubble Sort e Selection Sort apresentam ganhos mais modestos, pois sua estrutura sequencial limita o grau de paralelismo explorável.

O número ideal de threads depende tanto do algoritmo quanto do tamanho do dado: para dados pequenos, o overhead de criação de threads pode superar o ganho de paralelismo. Para dados grandes (≥ 50.000 elementos), 4 a 8 threads demonstram redução significativa de tempo para Merge Sort e Quick Sort.

A análise reforça que a escolha de algoritmos paralelos deve considerar: (1) a natureza do problema, (2) o tamanho dos dados, (3) a arquitetura do hardware disponível e (4) o custo de sincronização entre threads.

## Referências

CORMEN, T. H. et al. **Introdução aos Algoritmos**. 3. ed. Rio de Janeiro: Elsevier, 2012.

GOETZ, B. et al. **Java Concurrency in Practice**. Upper Saddle River: Addison-Wesley, 2006.

ORACLE. **Java SE Documentation: ForkJoinPool**. Disponível em: https://docs.oracle.com/en/java/docs/api/java.base/java/util/concurrent/ForkJoinPool.html. Acesso em: mai. 2026.

JFREECHART. **JFreeChart Developer Guide**. Disponível em: https://www.jfree.org/jfreechart/. Acesso em: mai. 2026.

KNUTH, D. E. **The Art of Computer Programming, Vol. 3: Sorting and Searching**. 2. ed. Reading: Addison-Wesley, 1998.

## Anexos

### Estrutura do Projeto

```
src/main/java/br/av2/
├── Main.java                          # Ponto de entrada (GUI ou CLI)
├── algorithms/
│   ├── serial/
│   │   ├── BubbleSort.java
│   │   ├── QuickSort.java
│   │   ├── MergeSort.java
│   │   └── SelectionSort.java
│   └── parallel/
│       ├── ParallelBubbleSort.java    # Odd-Even Transposition Sort
│       ├── ParallelQuickSort.java     # ForkJoinPool + RecursiveAction
│       ├── ParallelMergeSort.java     # ForkJoinPool + RecursiveAction
│       └── ParallelSelectionSort.java # ExecutorService + merge sequencial
├── benchmark/
│   ├── BenchmarkRunner.java           # Orquestrador de testes
│   ├── BenchmarkResult.java           # Modelo de resultado
│   └── DataGenerator.java             # Gerador de dados de entrada
├── csv/
│   └── CSVWriter.java                 # Exportador CSV
└── gui/
    ├── MainFrame.java                 # Janela principal Swing
    └── ChartPanelWrapper.java         # Gráficos dinâmicos JFreeChart
```

### Como Executar

```bash
# Compilar e empacotar
mvn package -q

# Executar com interface gráfica (padrão)
java -jar target/sort-benchmark-1.0-SNAPSHOT-jar-with-dependencies.jar

# Executar via linha de comando (sem GUI)
java -jar target/sort-benchmark-1.0-SNAPSHOT-jar-with-dependencies.jar --cli
```

### Link do GitHub

https://github.com/IIGabriel/java-concurrent-parallel-search
