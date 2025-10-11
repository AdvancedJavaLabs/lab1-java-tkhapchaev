package org.itmo;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

import java.util.Random;

@JCStressTest
@Outcome(id = "200000", expect = Expect.ACCEPTABLE, desc = "Все 50_000 вершин посещены 4 акторами по одному разу")
@State
public class JCStressBFSTest {
    private Graph graph = new RandomGraphGenerator().generateGraph(new Random(42), 50_000, 1_000_000);

    @Actor
    public void actor1() {
        graph.parallelBFS(0, Runtime.getRuntime().availableProcessors() / 4);
    }

    @Actor
    public void actor2() {
        graph.parallelBFS(0, Runtime.getRuntime().availableProcessors() / 4);
    }

    @Actor
    public void actor3() {
        graph.parallelBFS(0, Runtime.getRuntime().availableProcessors() / 4);
    }

    @Actor
    public void actor4() {
        graph.parallelBFS(0, Runtime.getRuntime().availableProcessors() / 4);
    }

    @Arbiter
    public void arbiter(I_Result result) {
        int visitedVerticesCounter = 0;

        for (int i = 0; i < graph.V; i++) {
            visitedVerticesCounter += graph.visitedParallel.get(i);
        }

        result.r1 = visitedVerticesCounter;
    }
}