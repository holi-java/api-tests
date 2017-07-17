package test.concurrent;

import org.junit.Test;

import java.util.concurrent.Exchanger;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static com.holi.utils.Task.spawn;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.matchers.ExceptionMatchers.throwing;
import static org.junit.Assert.assertThat;

public class ExchangerTest {
    private final Exchanger<Integer> exchanger = new Exchanger<>();

    @Test(timeout = 100)
    public void swapElementsWithinPairThreads() throws Throwable {
        Future<Integer> first = spawn(() -> exchanger.exchange(1));
        Future<Integer> second = spawn(() -> exchanger.exchange(2));

        assertThat(first.get(), equalTo(2));
        assertThat(second.get(), equalTo(1));
    }

    @Test
    public void throwsTimeoutExceptionWhenTimedOut() throws Throwable {
        assertThat(() -> exchanger.exchange(1, 100, MILLISECONDS), throwing(TimeoutException.class));
    }
}
