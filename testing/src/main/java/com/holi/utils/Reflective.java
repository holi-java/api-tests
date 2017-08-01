package com.holi.utils;

import java.util.concurrent.CompletionException;
import java.util.function.Function;

public class Reflective {
    public static <T, R> Function<T, R> reflectiveCall(RFunction<T, R> provider) {
        return it -> {
            try {
                return provider.apply(it);
            } catch (Throwable ex) {
                throw new CompletionException(ex);
            }
        };
    }

    public interface RFunction<T, R> {
        R apply(T value) throws Throwable;
    }
}
