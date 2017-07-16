package test.concurrent;

import org.junit.Test;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static com.holi.utils.ExConsumer.*;
import static com.holi.utils.Task.spawn;
import static com.holi.utils.Task.task;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.matchers.ExceptionMatchers.throwing;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CyclicBarrierTest {
    private boolean stateChanged;
    private CyclicBarrier barrier = new CyclicBarrier(2, () -> stateChanged = true);

    @Test
    public void allowsASetOfThreadsToWaitForEachOtherToReachACommonBarrierPoint() throws Throwable {
        blocking(lock -> blocking(before -> {
            Future<?> task1 = spawn(task(before::countDown).then(barrier::await));
            Future<?> task2 = spawn(task(lock::await).then(barrier::await));
            before.await();

            assertThat(() -> task1.get(100, MILLISECONDS), throwing(TimeoutException.class));
            assertThat(() -> task2.get(100, MILLISECONDS), throwing(TimeoutException.class));

            lock.countDown();
            task1.get(100, MILLISECONDS);
            task2.get(100, MILLISECONDS);
        }));
    }

    @Test
    public void canBeReusedAfterTheWaitingThreadAreReleased() throws Throwable {
        allowsASetOfThreadsToWaitForEachOtherToReachACommonBarrierPoint();
        allowsASetOfThreadsToWaitForEachOtherToReachACommonBarrierPoint();
    }

    @Test
    public void invokeActionForEachBarrierPointAfterTheLastThreadArrives() throws Throwable {
        blocking(lock -> blocking(before -> blocking(after -> {
            Future<?> task1 = spawn(task(before::countDown).then(barrier::await).then(after::await));
            Future<?> task2 = spawn(task(lock::await).then(() -> barrier.await()));

            before.await();

            assertFalse(stateChanged);

            lock.countDown();
            task2.get(100, MILLISECONDS);
            assertTrue(stateChanged);

            after.countDown();
            task1.get(100, MILLISECONDS);
        })));
    }

    @Test
    public void barrierBrokenWhenTimeout() throws Throwable {
        blocking(before -> {
            Future<?> task = spawn(task(before::countDown).then(() -> barrier.await(100, MILLISECONDS)));
            assertTrue(before.await(100, MILLISECONDS));

            assertThat(barrier.getNumberWaiting(), equalTo(1));
            assertThat(barrier.getParties(), equalTo(2));
            assertFalse(barrier.isBroken());

            assertThat(task::get, throwing(ExecutionException.class));

            assertThat(barrier.getNumberWaiting(), equalTo(0));
            assertThat(barrier.getParties(), equalTo(2));
            assertTrue(barrier.isBroken());
        });
    }
}
