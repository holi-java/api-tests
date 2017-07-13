package test.concurrent;

import org.junit.Test;

import java.util.concurrent.*;

import static java.lang.Thread.sleep;
import static java.util.concurrent.Executors.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@SuppressWarnings("WeakerAccess")
public class CompletionServiceTest {

    final CompletionService<Integer> completionService = new ExecutorCompletionService<>(newWorkStealingPool());

    Integer take() throws InterruptedException, ExecutionException {
        return completionService.take().get();
    }

    @Test
    public void returnTheValueWhenTheRunnableTaskIsCompleted() throws Throwable {
        completionService.submit(() -> {/**/}, 1);

        assertThat(take(), equalTo(1));
    }

    @Test
    public void returnTheValueWhenTheCallableTaskIsCompleted() throws Throwable {
        completionService.submit(() -> 2);

        assertThat(take(), equalTo(2));
    }

    @Test(timeout = 300)
    public void producesResultsInParallelReliesOnTheExecutor() throws Throwable {
        completionService.submit(delayed(200, () -> 1));
        completionService.submit(delayed(200, () -> 2));

        assertThat(take(), equalTo(1));
        assertThat(take(), equalTo(2));
    }

    @Test
    public void producesTheResultOfTheFirstCompletedTask() throws Throwable {
        completionService.submit(delayed(100, () -> 1));
        completionService.submit(() -> 2);

        assertThat(take(), equalTo(2));
        assertThat(take(), equalTo(1));
    }

    private <V> Callable<V> delayed(long millis, Callable<V> target) {
        return () -> {
            sleep(millis);
            return target.call();
        };
    }
}
