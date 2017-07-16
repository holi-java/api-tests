package com.holi.utils;

import java.util.concurrent.CountDownLatch;

public interface Execution<T> {
    static void blocking(Execution<CountDownLatch> action) throws Exception {
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

    static Task releasingBefore(CountDownLatch blocking, Task target) {
        return () -> {
            blocking.countDown();
            target.run();
        };
    }

    static Task releasingAfter(CountDownLatch blocking, Task target) {
        return () -> {
            target.run();
            blocking.countDown();
        };
    }

    void accept(T value) throws Exception;
}
