package test.concurrent;

import org.hamcrest.matchers.ExceptionMatchers.ExceptionExpectation;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static com.holi.utils.Task.spawn;
import static com.holi.utils.Task.task;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.matchers.ExceptionMatchers.throwing;
import static org.junit.Assert.assertThat;

public class CountDownLatchTest {

    @Test
    public void waitsUtilTheCountReachesToZero() throws Throwable {
        CountDownLatch counter = new CountDownLatch(2);
        Future<?> task = spawn(task(counter::await));

        ExceptionExpectation action = () -> task.get(100, MILLISECONDS);

        assertThat(action, throwing(TimeoutException.class));

        counter.countDown();
        assertThat(action, throwing(TimeoutException.class));

        counter.countDown();
        action.run();
    }
}
