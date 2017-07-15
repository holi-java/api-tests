package test.concurrent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.holi.utils.Task.spawn;
import static com.holi.utils.Tasks.delayed;
import static java.util.concurrent.Executors.*;
import static java.util.concurrent.Executors.newWorkStealingPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.matchers.ExceptionMatchers.throwing;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class ExecutorServiceTest {
    private final ExecutorService executorService;

    public ExecutorServiceTest(Supplier<ExecutorService> factory) {
        this.executorService = factory.get();
    }

    @Parameters
    public static Collection<Supplier<ExecutorService>> executors() {
        return Arrays.asList(
                Executors::newCachedThreadPool,
                () -> newFixedThreadPool(8),
                Executors::newSingleThreadExecutor,
                Executors::newWorkStealingPool,
                () -> newWorkStealingPool(8)
        );
    }

    @Test
    public void allowsPreviouslySubmittedTasksToExecuteBeforeTerminatedByShutdown() throws Throwable {
        Future<Integer> task = executorService.submit(delayed(() -> 1));

        executorService.shutdown();

        assertTrue(executorService.isShutdown());
        assertThat(task.get(), equalTo(1));
    }

    @Test
    public void rejectNewTasksWhenExecutorIsShutdown() throws Throwable {
        executorService.shutdown();

        assertThat(() -> executorService.submit(() -> 1), throwing(RejectedExecutionException.class));
    }

    @Test
    public void preventsWaitingTasksFromStarting_and_AttemptToStopCurrentlyExecutingTasksWhenTerminatedByShutdownNow() throws Throwable {
        Future<Integer> task = executorService.submit(delayed(() -> 1));

        executorService.shutdownNow();

        assertThat(task::get, either(throwing(ExecutionException.class)).or(throwing(CancellationException.class)));
    }

    @Test
    public void hasNoActivelyExecutingTasksUponTermination() throws Throwable {
        tasks(10).forEach(executorService::submit);

        executorService.shutdownNow();

        assertTrue(executorService.isShutdown());

        executorService.awaitTermination(100, MILLISECONDS);
        assertTrue(executorService.isTerminated());
    }


    @Test
    public void invokeAllTasksAndWaitingUntilAllTasksWereCompleted() throws Throwable {
        executorService.invokeAll(tasks(5).collect(toList())).forEach(it -> {
            assertThat(it, hasProperty("done", is(true)));
        });
    }

    @Test(timeout = 200)
    public void invokeAnyTasksWereCompletedAndCancelOtherTasks() throws Throwable {
        Integer result = executorService.invokeAny(tasks(5).collect(toList()));

        assertThat(result, oneOf(0, 1, 2, 3, 4));
    }

    private Stream<Callable<Integer>> tasks(int count) {
        return IntStream.range(0, count).mapToObj(i -> delayed(() -> i));
    }
}
