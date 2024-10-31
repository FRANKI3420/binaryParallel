import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;

class binaryParallel {
    static final int finish = 10000000;
    static final int sleep = 0;

    public static CompletableFuture<Void> traverseParallel(ExecutorService executorService, int index) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            System.err.println("Task interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }

        if (index >= finish) {
            return CompletableFuture.completedFuture(null);
        }

        // System.out.println("Processing Node " + index + " on Thread ID: " +
        // Thread.currentThread().getId());

        // Async left and right node processing
        CompletableFuture<Void> leftTask = CompletableFuture
                .supplyAsync(() -> traverseParallel(executorService, 2 * index), executorService)
                .thenCompose(future -> future);
        CompletableFuture<Void> rightTask = CompletableFuture
                .supplyAsync(() -> traverseParallel(executorService, 2 * index + 1), executorService)
                .thenCompose(future -> future);

        // 左右の子タスクが完了するのを待機
        return CompletableFuture.allOf(leftTask, rightTask);

        // // 左右の子ノードを非同期で処理
        // CompletableFuture<Void> leftTask = CompletableFuture.runAsync(() ->
        // traverseParallel(executorService, 2 * index), executorService);
        // CompletableFuture<Void> rightTask = CompletableFuture.runAsync(() ->
        // traverseParallel(executorService, 2 * index + 1), executorService);

        // // 左右の子タスクの完了を待機せずに返す
        // return CompletableFuture.completedFuture(null);
    }

    public static void traverse(int index) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            System.err.println("Task interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }

        if (index >= finish) {
            return;
        }

        // System.out.println("Node " + index);

        // 左の子を並列で処理
        traverse(2 * index);

        // 右の子を並列で処理
        traverse(2 * index + 1);
    }

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        System.out.println("finish:" + finish + " sleep:" + sleep);
        System.out.println("逐次");
        long time = System.nanoTime();
        traverse(1);
        time = System.nanoTime() - time;
        System.out.println((double) (time) / 1000 / 1000 + "ms");
        System.out.println((double) (time) / 1000 / 1000 / 1000 + "s");

        System.out.println("並列");
        time = System.nanoTime();
        try {
            traverseParallel(executorService, 1).join(); // すべてのタスクが完了するのを待機
        } finally {
            time = System.nanoTime() - time;
            try {
                executorService.shutdown();
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                e.printStackTrace();
            }
            System.out.println((double) (time) / 1000 / 1000 + "ms");
            System.out.println((double) (time) / 1000 / 1000 / 1000 + "s");
        }
    }
}