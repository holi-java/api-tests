package test.concurrent;

import org.junit.Test;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SemaphoreTest {

    private final Semaphore semaphore = new Semaphore(0);

    @Test
    public void awaitWhenNoPermitsAvailable() throws Throwable {
        assertThat(semaphore.drainPermits(), equalTo(0));
        assertThat(semaphore.tryAcquire(100, TimeUnit.MILLISECONDS), is(false));

        semaphore.release();
        assertThat(semaphore.availablePermits(), equalTo(1));

        semaphore.acquire();
    }

    @Test
    public void acquire1PermitAndReturnAllPermits() throws Throwable {
        semaphore.release();

        assertThat(semaphore.drainPermits(), equalTo(1));

        assertThat(semaphore.tryAcquire(100, TimeUnit.MILLISECONDS), is(false));
    }

}
