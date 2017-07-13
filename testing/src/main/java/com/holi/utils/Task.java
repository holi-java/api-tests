package com.holi.utils;

import java.util.concurrent.*;
import java.util.function.Consumer;


public interface Task {

    ExecutorService EXECUTOR = Executors.newWorkStealingPool(20);

    void run() throws Exception;

    static void blocking(Consumer<CountDownLatch> action) {
        CountDownLatch blocking = new CountDownLatch(1);
        try {
            action.accept(blocking);
        } finally {
            blocking.countDown();
        }
    }

    static Task await(CountDownLatch blocking, Task target) {
        return () -> {
            blocking.await();
            target.run();
        };
    }

    static Task releasing(CountDownLatch blocking, Task target) {
        return () -> {
            blocking.countDown();
            target.run();
        };
    }

    static <T> Future<T> delay(Callable<T> task) {
        return spawn(() -> {
            Thread.sleep(100);
            return task.call();
        });
    }

    static <T> Future<T> spawn(Callable<T> task) {
        return EXECUTOR.submit(task);
    }

    static Future<?> spawn(Task task) {
        return spawn((Callable<?>) () -> {
            task.run();
            return null;
        });
    }
}
