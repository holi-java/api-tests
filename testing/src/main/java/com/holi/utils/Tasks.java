package com.holi.utils;

import java.util.concurrent.Callable;

public class Tasks {
    public static <V> Callable<V> delayed(Callable<V> task) {
        return () -> {
            Thread.sleep(100);
            return task.call();
        };
    }
}
