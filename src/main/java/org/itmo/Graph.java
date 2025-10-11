package org.itmo;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicIntegerArray;

class Graph {
    public final int V;
    public final ArrayList<Integer>[] adjList;

    public boolean[] visitedSerial;
    public AtomicIntegerArray visitedParallel;

    Graph(int vertices) {
        this.V = vertices;
        adjList = new ArrayList[vertices];

        for (int i = 0; i < vertices; ++i) {
            adjList[i] = new ArrayList<>();
        }

        visitedSerial = new boolean[V];
        visitedParallel = new AtomicIntegerArray(V);
    }

    void addEdge(int src, int dest) {
        if (!adjList[src].contains(dest)) {
            adjList[src].add(dest);
        }
    }

    void parallelBFS(int startVertex, int numberOfThreads) {
        AtomicIntegerArray visited = new AtomicIntegerArray(V);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        ConcurrentLinkedQueue<Integer> currentLevel = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < V; i++) {
            visited.set(i, 0);
        }

        currentLevel.add(startVertex);
        visited.set(startVertex, 1);

        try {
            while (!currentLevel.isEmpty()) {
                int numberOfTasks = numberOfThreads;
                int currentLevelSize = currentLevel.size();

                if (currentLevelSize < numberOfTasks) {
                    numberOfTasks = currentLevelSize;
                }

                CountDownLatch countDownLatch = new CountDownLatch(numberOfTasks);
                List<Integer>[] nextLevels = new ArrayList[numberOfTasks];

                for (int i = 0; i < numberOfTasks; i++) {
                    int taskNumber = i;

                    executorService.submit(() -> {
                        List<Integer> nextLevel = new ArrayList<>();

                        try {
                            Integer vertex;

                            while ((vertex = currentLevel.poll()) != null) {
                                for (int neighbor : adjList[vertex]) {
                                    if (visited.get(neighbor) == 0 && visited.compareAndSet(neighbor, 0, 1)) {
                                        nextLevel.add(neighbor);
                                    }
                                }
                            }
                        } finally {
                            nextLevels[taskNumber] = nextLevel;
                            countDownLatch.countDown();
                        }
                    });
                }

                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                for (int i = 0; i < numberOfTasks; i++) {
                    currentLevel.addAll(nextLevels[i]);
                }
            }
        } finally {
            executorService.shutdown();
        }

        for (int i = 0; i < V; i++) {
            if (visited.get(i) == 1) {
                visitedParallel.getAndIncrement(i);
            }
        }
    }

    void parallelBFSWithUnsafeIncrement(int startVertex, int numberOfThreads) {
        AtomicIntegerArray visited = new AtomicIntegerArray(V);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        ConcurrentLinkedQueue<Integer> currentLevel = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < V; i++) {
            visited.set(i, 0);
        }

        currentLevel.add(startVertex);
        visited.set(startVertex, 1);

        try {
            while (!currentLevel.isEmpty()) {
                int numberOfTasks = numberOfThreads;
                int currentLevelSize = currentLevel.size();

                if (currentLevelSize < numberOfTasks) {
                    numberOfTasks = currentLevelSize;
                }

                CountDownLatch countDownLatch = new CountDownLatch(numberOfTasks);
                List<Integer>[] nextLevels = new ArrayList[numberOfTasks];

                for (int i = 0; i < numberOfTasks; i++) {
                    int taskNumber = i;

                    executorService.submit(() -> {
                        List<Integer> nextLevel = new ArrayList<>();

                        try {
                            Integer vertex;

                            while ((vertex = currentLevel.poll()) != null) {
                                for (int neighbor : adjList[vertex]) {
                                    if (visited.get(neighbor) == 0 && visited.compareAndSet(neighbor, 0, 1)) {
                                        nextLevel.add(neighbor);
                                    }
                                }
                            }
                        } finally {
                            nextLevels[taskNumber] = nextLevel;
                            countDownLatch.countDown();
                        }
                    });
                }

                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                for (int i = 0; i < numberOfTasks; i++) {
                    currentLevel.addAll(nextLevels[i]);
                }
            }
        } finally {
            executorService.shutdown();
        }

        for (int i = 0; i < V; i++) {
            if (visited.get(i) == 1) {
                int currentValue = visitedParallel.get(i);
                visitedParallel.set(i, currentValue + 1);
            }
        }
    }

    //Generated by ChatGPT
    void bfs(int startVertex) {
        boolean[] visited = new boolean[V];

        LinkedList<Integer> queue = new LinkedList<>();

        visited[startVertex] = true;
        queue.add(startVertex);

        while (!queue.isEmpty()) {
            startVertex = queue.poll();

            for (int n : adjList[startVertex]) {
                if (!visited[n]) {
                    visited[n] = true;
                    queue.add(n);
                }
            }
        }

        visitedSerial = visited;
    }
}