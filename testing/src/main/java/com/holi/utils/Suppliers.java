package com.holi.utils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class Suppliers {
    //@formatter:off
    private interface Lazily<T> extends Supplier<T> {/**/}
    //@formatter:on

    public static <T> Supplier<T> lazy(Supplier<T> provider) {
        if (requireNonNull(provider) instanceof Lazily) return provider;

        return new Lazily<T>() {
            private Supplier<T> delegate = () -> persisted(provider).get();

            private Supplier<T> persisted(Supplier<T> target) {
                T value = target.get();
                return this.delegate = () -> value;
            }

            @Override
            public T get() {
                return delegate.get();
            }
        };
    }

    public static <T> Supplier<T> lazy(Lock lock, Supplier<T> provider) {
        return lazy(lock(lock, lazy(provider)));
    }

    public static <T> Supplier<T> lock(Lock lock, Supplier<T> provider) {
        requireNonNull(lock);
        return () -> {
            lock.lock();
            try {
                return provider.get();
            } finally {
                lock.unlock();
            }
        };
    }

    @SafeVarargs
    public static Supplier<String> provides(Supplier<String>... providers) {
        Iterator<Supplier<String>> generator = Arrays.asList(providers).iterator();

        return () -> generator.next().get();
    }

}
