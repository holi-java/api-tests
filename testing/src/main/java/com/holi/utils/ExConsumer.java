package com.holi.utils;

import java.util.concurrent.CountDownLatch;

public interface ExConsumer<T> {
    static void blocking(ExConsumer<CountDownLatch> action) throws Exception {
        CountDownLatch blocking = new CountDownLatch(1);
        try {
            action.accept(blocking);
        } finally {
            blocking.countDown();
        }
    }

    void accept(T value) throws Exception;
}
