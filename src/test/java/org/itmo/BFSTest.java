package org.itmo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.util.HashSet;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

public class BFSTest {

    @Test
    public void bfsTest() throws IOException {
        int[] sizes = new int[]{10, 100, 1000, 10_000, 10_000, 50_000, 100_000, 1_000_000, 2_000_000, 2_500_000};
        int[] connections = new int[]{50, 500, 5000, 50_000, 100_000, 1_000_000, 1_000_000, 10_000_000, 10_000_000, 10_500_000};
        Random r = new Random(42);
        try (FileWriter fw = new FileWriter("tmp/results.txt")) {
            for (int i = 0; i < sizes.length; i++) {
                System.out.println("----------------------------------------------------");
                System.out.println("Generating graph with size " + sizes[i] + "...");
                Graph g = new RandomGraphGenerator().generateGraph(r, sizes[i], connections[i]);
                System.out.println("Generation completed!\nStarting bfs...");

                long serialTime = executeSerialBfsAndGetTime(g);
                long parallelTime = executeParallelBfsAndGetTime(g, Runtime.getRuntime().availableProcessors());
                System.out.println("Bfs completed!");
                System.out.println("Checking that all vertices have been visited...");

                for (int j = 0; j < g.visitedSerial.length; j++) {
                    Assertions.assertEquals(true, g.visitedSerial[j]);
                }

                for (int j = 0; j < g.visitedParallel.length; j++) {
                    Assertions.assertEquals(true, g.visitedParallel[j].get());
                }

                String winner = serialTime > parallelTime ? "parallel algorithm" : "serial algorithm";
                double gain = serialTime > parallelTime ? (double) serialTime / parallelTime : (double) parallelTime / serialTime;
                String gainFormatted = gain == Double.POSITIVE_INFINITY ? "incomparably faster" : String.format("%.2f", gain) + " times faster";
                String total = serialTime == parallelTime ? "algorithms are equal" : winner + " is " + gainFormatted;

                fw.append("Times for " + sizes[i] + " vertices and " + connections[i] + " connections: ");
                fw.append("\nSerial: " + serialTime + " ms");
                fw.append("\nParallel: " + parallelTime + " ms");
                fw.append("\nTotal: " + total);
                fw.append("\n----------------------------------------------------\n");
            }
            fw.flush();
        }
    }

    private long executeSerialBfsAndGetTime(Graph g) {
        long startTime = System.currentTimeMillis();
        g.bfs(0);
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private long executeParallelBfsAndGetTime(Graph g, int numberOfThreads) {
        long startTime = System.currentTimeMillis();
        g.parallelBFS(0, numberOfThreads);
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }
}
