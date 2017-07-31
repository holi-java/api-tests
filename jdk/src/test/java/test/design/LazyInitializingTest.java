package test.design;

import org.junit.Test;

import java.util.function.Supplier;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class LazyInitializingTest {

    @Test
    public void initializingOnce() throws Throwable {
        Supplier<String> foo = lazy(() -> new String(new char[]{'b', 'a', 'r'}));

        assertThat(foo.get(), sameInstance(foo.get()));
    }

    private <T> Supplier<T> lazy(Supplier<T> provider) {
        T value = provider.get();
        return () -> value;
    }
}
