package test.concurrent;

import org.junit.Test;

import java.util.Set;
import java.util.concurrent.*;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@SuppressWarnings("WeakerAccess")
public class ExecutorTest {
    final Set<Thread> created = new CopyOnWriteArraySet<>();
    final Executor executor = newFixedThreadPool(3, this::thread);

    private Thread thread(Runnable task) {
        Thread thread = new Thread(task);
        created.add(thread);
        return thread;
    }


    @Test
    public void executeTaskInASeparatedThread() throws Throwable {
        BlockingQueue<Thread> queue = new ArrayBlockingQueue<>(1);

        executor.execute(collectWorkingThread(queue));
        executor.execute(collectWorkingThread(queue));


        assertThat(queue.take(), in(created));
        assertThat(queue.take(), in(created));
        assertThat(queue.poll(100, MILLISECONDS), is(nullValue()));
    }

    private Runnable collectWorkingThread(BlockingQueue<Thread> queue) {
        return () -> {
            try {
                queue.put(Thread.currentThread());
            } catch (InterruptedException e) {
                throw new CompletionException(e);
            }
        };
    }

}
