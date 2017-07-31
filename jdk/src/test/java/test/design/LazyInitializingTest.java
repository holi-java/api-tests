package test.design;

import org.junit.Test;

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

    private <T> T fail() {
        throw new IllegalStateException();
    }

    private <T> Supplier<T> lazy(Supplier<T> provider) {
        @SuppressWarnings("unchecked")
        T[] value = (T[]) new Object[1];
        return () -> value[0] != null ? value[0] : (value[0] = provider.get());
    }
}
