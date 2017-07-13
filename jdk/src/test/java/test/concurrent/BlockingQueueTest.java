package test.concurrent;

import com.holi.utils.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.*;
import java.util.function.IntFunction;

import static com.holi.utils.Task.await;
import static com.holi.utils.Task.spawn;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.matchers.ExceptionMatchers.throwing;
import static org.hamcrest.matchers.ExceptionMatchers.withMessage;
import static org.junit.Assert.assertThat;

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class BlockingQueueTest {
    final int capacity = 1;
    final BlockingQueue<Integer> queue;

    public BlockingQueueTest(IntFunction<BlockingQueue<Integer>> factory) {
        queue = factory.apply(capacity);
    }

    @Parameters
    public static Collection<IntFunction<BlockingQueue<Integer>>> parameters() {
        return asList(
                ArrayBlockingQueue::new,
                LinkedBlockingQueue::new,
                LinkedBlockingDeque::new
        );
    }

    @Test
    public void cannotAddingNullValueIntoTheBlockingQueue() throws Throwable {
        assertThat(() -> queue.add(null), throwing(NullPointerException.class));
    }


    @Test
    public void cannotAddingValueIntoAFullyQueue() throws Throwable {
        fill(queue);

        assertThat(() -> queue.add(1), throwing(IllegalStateException.class));
    }

    @Test
    public void pollNullFromAnEmptyQueue() throws Throwable {
        assertThat(queue.poll(), is(nullValue()));
    }

    @Test
    public void pollDonotBlockingToWaitingTheResult() throws Throwable {
        Future<?> task = Task.delay(() -> queue.add(1));

        assertThat(queue.poll(), is(nullValue()));

        task.get();
        assertThat(queue.poll(), equalTo(1));
    }

    @Test
    public void takeWillBlockingToWaitingTheResult() throws Throwable {
        Task.delay(() -> queue.add(2));

        assertThat(queue.take(), equalTo(2));
    }

    @Test(timeout = 200)
    public void pollWaitUntilQueueIsNotEmpty() throws Throwable {
        Task.delay(() -> queue.add(2));

        assertThat(queue.poll(1, DAYS), equalTo(2));
    }

    @Test
    public void donotThrowsNoSuchElementExceptionWhenPeekingAnEmptyQueue() throws Throwable {
        assertThat(queue::peek, not(throwing(NoSuchElementException.class)));
    }

    @Test
    public void throwsNoSuchElementExceptionWhenRetrieveTheFirstElementFromAnEmptyQueue() throws Throwable {
        assertThat(queue::element, throwing(NoSuchElementException.class));
    }

    @Test
    public void putWillWaitingForAFullyQueue() throws Throwable {
        fill(queue);
        CountDownLatch blocking = new CountDownLatch(1);

        spawn(Task.releasing(blocking, () -> queue.put(2)));

        blocking.await();

        assertThat(queue.take(), equalTo(0));
        assertThat(queue.take(), equalTo(2));
    }


    @Test
    public void addAllMakesQueueOverflow() throws Throwable {
        List<Integer> overflowed = range(0, capacity + 1).boxed().collect(toList());

        assertThat(() -> queue.addAll(overflowed), throwing(IllegalStateException.class, withMessage(containsString("full"))));
    }

    @Test
    public void cancel() throws Throwable {
        Task.blocking(it -> spawn(await(it, () -> queue.add(1))).cancel(true));

        Integer value = queue.poll(200, MILLISECONDS);

        assertThat(value, anyOf(is(1), nullValue()));
    }


    private void fill(BlockingQueue<Integer> queue) {
        range(0, queue.remainingCapacity()).forEach(queue::add);
    }
}
