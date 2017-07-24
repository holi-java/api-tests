package test.concurrent;

import org.junit.Test;

import java.util.concurrent.Future;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeoutException;

import static com.holi.utils.ExConsumer.blocking;
import static com.holi.utils.Task.spawn;
import static com.holi.utils.Task.task;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.matchers.ExceptionMatchers.throwing;
import static org.junit.Assert.assertThat;

public class PhaserTest {

    private Phaser phaser = new Phaser();


    @Test
    public void arriveDonNotBlockingButReturnAnAssociatePhaseNumber() throws Throwable {
        phaser.bulkRegister(2);

        assertThat(phaser.arrive(), equalTo(0));
        assertThat(phaser.arrive(), equalTo(0));

        assertThat(phaser.arriveAndDeregister(), equalTo(1));
        assertThat(phaser.arriveAndDeregister(), equalTo(1));
    }

    @Test
    public void allSynchronizationMethodsImmediatelyReturnANegativePhaseNumberAfterTermination() throws Throwable {
        phaser.forceTermination();

        assertThat(phaser.arriveAndAwaitAdvance(), lessThan(0));
    }

    @Test
    public void runAsCountLatch() throws Throwable {
        phaser.register();//register self
        phaser.register();

        Future<Integer> task = spawn(phaser::arriveAndAwaitAdvance);
        assertThat(() -> task.get(100, MILLISECONDS), throwing(TimeoutException.class));

        phaser.register();
        assertThat(phaser.arriveAndDeregister(), equalTo(0));
        assertThat(() -> task.get(100, MILLISECONDS), throwing(TimeoutException.class));

        assertThat(phaser.arriveAndDeregister(), equalTo(0));
        assertThat(task.get(100, MILLISECONDS), equalTo(1));
    }

    @Test
    public void awaitRepeatedlyAsCyclicBarrier() throws Throwable {
        phaser.bulkRegister(2);

        Future<Integer> task1 = spawn(phaser::arriveAndAwaitAdvance);
        Future<Integer> task2 = spawn(phaser::arriveAndAwaitAdvance);
        assertThat(task1.get(100, MILLISECONDS), greaterThan(0));
        assertThat(task2.get(100, MILLISECONDS), greaterThan(0));

        Future<Integer> task3 = spawn(phaser::arriveAndAwaitAdvance);
        assertThat(() -> task3.get(100, MILLISECONDS), throwing(TimeoutException.class));

        Future<Integer> task4 = spawn(phaser::arriveAndAwaitAdvance);
        assertThat(task3.get(100, MILLISECONDS), greaterThan(0));
        assertThat(task4.get(100, MILLISECONDS), greaterThan(0));
    }

    @Test
    public void thePhaseNumberMayBeChangedOverTime() throws Throwable {
        phaser.bulkRegister(2);

        Future<Integer> task1 = spawn(phaser::arriveAndAwaitAdvance);
        assertThat(() -> task1.get(100, MILLISECONDS), throwing(TimeoutException.class));

        phaser.register();
        phaser.arriveAndDeregister();
        assertThat(() -> task1.get(100, MILLISECONDS), throwing(TimeoutException.class));

        phaser.arriveAndDeregister();
        assertThat(task1.get(100, MILLISECONDS), greaterThan(0));
    }

    @Test
    public void arriveFailsIfNoParties() throws Throwable {
        assertThat(phaser::arrive, throwing(IllegalStateException.class));
        assertThat(phaser::arriveAndDeregister, throwing(IllegalStateException.class));

        phaser.register();

        for (any of : times) {
            phaser.arrive();
            phaser.arriveAndDeregister();
            phaser.arriveAndAwaitAdvance();
        }
    }

    @Test
    public void continueToWaitEvenIfTheWaitingThreadIsInterrupted() throws Throwable {
        phaser.bulkRegister(2);

        blocking(lock -> {
            Future<?> task = spawn(task(phaser::arriveAndAwaitAdvance).then(lock::countDown));

            task.cancel(true);
            assertThat(task.isCancelled(), is(true));
            assertThat(task.isDone(), is(true));

            phaser.arriveAndDeregister();
            assertThat(lock.await(100, MILLISECONDS), is(true));
        });
    }

    static any[] times = new any[1];

    interface any {
    }
}

