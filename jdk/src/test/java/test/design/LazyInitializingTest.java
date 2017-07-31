package test.design;

import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.matchers.ExceptionMatchers.throwing;
import static org.junit.Assert.assertThat;

public class LazyInitializingTest {

    @Test
    public void initializingOnce() throws Throwable {
        Supplier<String> foo = lazy(() -> new String(new char[]{'b', 'a', 'r'}));

        assertThat(foo.get(), sameInstance(foo.get()));
    }

    @Test
    public void lazyInitialized() throws Throwable {
        Supplier<String> foo = lazy(this::fail);

        assertThat(foo::get, throwing(IllegalStateException.class));
    }

    @Test
    public void initializingNullValueOnce() throws Throwable {
        Supplier<String> foo = lazy(provideAs(() -> null, this::fail));

        assertThat(foo.get(), sameInstance(foo.get()));
    }

    private <T> Supplier<T> lazy(Supplier<T> provider) {
        return new Supplier<T>() {
            Supplier<T> delegate = () -> persisted(provider).get();

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

    @SafeVarargs
    private final Supplier<String> provideAs(Supplier<String>... suppliers) {
        Iterator<Supplier<String>> providers = Arrays.asList(suppliers).iterator();

        return () -> providers.next().get();
    }

    private <T> T fail() {
        throw new IllegalStateException();
    }
}
