package test.concurrent;

import org.junit.Test;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

import static com.holi.utils.Execution.releasingAfter;
import static com.holi.utils.Execution.blocking;
import static com.holi.utils.Task.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class TransferQueueTest {
    private final TransferQueue<Integer> queue = new LinkedTransferQueue<>();

    @Test
    public void transferWaitingForReceiptUntilElementWasConsumed() throws Throwable {
        blocking(it -> {
            spawn(releasingAfter(it, () -> queue.transfer(1)));
            assertFalse(it.await(100, MILLISECONDS));

            assertThat(queue.take(), equalTo(1));
            assertTrue(it.await(100, MILLISECONDS));
        });
    }

    @Test(timeout = 100)
    public void tryTransferWithoutWaitingForReceipt() throws Throwable {
        queue.tryTransfer(1);
    }

    @Test(timeout = 200)
    public void tryTransferWaitingForReceiptTimeout() throws Throwable {
        queue.tryTransfer(1, 100, MILLISECONDS);
    }
}
