package com.holi.utils;

import java.util.concurrent.*;


public interface Task {
    int POOL_SIZE = 20;
    ExecutorService EXECUTOR = Executors.newWorkStealingPool(POOL_SIZE);

    static Task task(Task task) {
        return task;
    }

    default Task then(Task task) {
        return () -> {
            run();
            task.run();
        };
    }

    default Task after(Task task) {
        return () -> {
            task.run();
            run();
        };
    }

    void run() throws Exception;

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
