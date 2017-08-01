package test.design;

import com.holi.utils.Task;
import com.holi.utils.any;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.holi.utils.Reflective.reflectiveCall;
import static com.holi.utils.any.times;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.matchers.ExceptionMatchers.throwing;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class LazyInitializingTest {

    @Test
    public void initializingOnce() throws Throwable {
        Supplier<String> foo = lazy(provides(() -> "foo", this::fail));

        assertThat(foo.get(), sameInstance(foo.get()));
    }

    @Test
    public void lazyInitialized() throws Throwable {
        Supplier<String> foo = lazy(this::fail);

        assertThat(foo::get, throwing(IllegalStateException.class));
    }

    @Test
    public void initializingNullValueOnce() throws Throwable {
        Supplier<String> foo = lazy(provides(() -> null, this::fail));

        assertThat(foo.get(), sameInstance(foo.get()));
    }

    @Test
    public void initializingSynchronized() throws Throwable {
        ReentrantLock lock = new ReentrantLock(true);
        for (@SuppressWarnings("unused") any of : times) {
            Supplier<String> foo = lazy(lock, provides(() -> "bar", this::fail));

            long count = spawn(100, foo::get).stream().map(reflectiveCall(Future::get)).distinct().count();

            assertThat(count, equalTo(1L));
        }
    }

    private <T> Supplier<T> lazy(Lock lock, Supplier<T> provider) {
        Supplier<T> lazily = lazy(provider);
        return lazy(() -> {
            lock.lock();
            try {
                return lazily.get();
            } finally {
                lock.unlock();
            }
        });
    }

    private <T> Supplier<T> lazy(Supplier<T> provider) {
        return new Supplier<T>() {
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

    private <T> List<Future<T>> spawn(int times, Callable<T> task) throws InterruptedException {
        int poolSize = Math.min(20, Task.POOL_SIZE);
        CountDownLatch lock = new CountDownLatch(1);
        CountDownLatch block = new CountDownLatch(poolSize);
        Supplier<Future<T>> provider = () -> Task.spawn(() -> {
            block.countDown();
            lock.await();
            return task.call();
        });
        try {
            return IntStream.range(0, times).parallel().mapToObj(i -> provider.get()).collect(Collectors.toList());
        } finally {
            assertTrue(block.await(500, TimeUnit.MILLISECONDS));
            lock.countDown();
        }
    }

    @SafeVarargs
    private final Supplier<String> provides(Supplier<String>... suppliers) {
        Iterator<Supplier<String>> providers = Arrays.asList(suppliers).iterator();

        return () -> providers.next().get();
    }

    private <T> T fail() {
        throw new IllegalStateException();
    }
}
