package test.nio;

import org.junit.Before;
import org.junit.Test;

import java.nio.*;

import static java.nio.ByteOrder.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.matchers.ExceptionMatchers.throwing;
import static org.junit.Assert.*;

public class BufferTest {
    private final ByteBuffer buff = ByteBuffer.allocate(3);

    @Before
    public void fill() {
        buff.put(new byte[]{1, 2, 3}).clear();
    }

    @Test
    public void startingValues() throws Throwable {
        assertThat(buff.capacity(), equalTo(3));
        assertThat(buff.position(), equalTo(0));
        assertThat(buff.limit(), equalTo(3));
        assertFalse(buff.isDirect());
        assertFalse(buff.isReadOnly());
        assertTrue(buff.hasArray());
    }

    @Test
    public void arrayOffset() throws Throwable {
        assertThat(buff.arrayOffset(), equalTo(0));

        ByteBuffer offset = ByteBuffer.wrap(buff.array(), 1, 2);

        assertThat(offset.arrayOffset(), equalTo(0));
        assertThat(offset.array(), equalTo(new byte[]{1, 2, 3}));
        assertThat(offset.get(), equalTo((byte) 2));
    }

    @Test
    public void capacityNeverBeChanged() throws Throwable {
        assertThat(buff.limit(2).capacity(), equalTo(3));
    }

    @Test
    public void limitCanBeGreaterThanItsCapacity() throws Throwable {
        assertThat(() -> buff.limit(buff.capacity() + 1), throwing(IllegalArgumentException.class));
    }

    @Test
    public void canNotReadOrWriteOverALimitedBuffer() throws Throwable {
        int positionOverLimit = 2;
        buff.limit(positionOverLimit);
        assertThat(buff.get(1), equalTo((byte) 2));

        assertThat(() -> buff.get(positionOverLimit), throwing(IndexOutOfBoundsException.class));
        assertThat(() -> buff.put(positionOverLimit, (byte) -1), throwing(IndexOutOfBoundsException.class));
    }

    @Test
    public void positionIndicateTheNextElementPositionInBuffer() throws Throwable {
        assertThat(buff.get(), equalTo((byte) 1));

        buff.position(2);
        assertThat(buff.get(), equalTo((byte) 3));
    }

    @Test
    public void resetPosition() throws Throwable {
        buff.mark();
        buff.position(2);

        buff.reset();

        assertThat(buff.position(), equalTo(0));
    }

    @Test
    public void remark() throws Throwable {
        buff.mark();

        buff.position(1);
        buff.mark();

        buff.position(2);
        buff.reset();

        assertThat(buff.position(), equalTo(1));
    }

    @Test
    public void clearMarkWhenScrollToPreviousPosition() throws Throwable {
        buff.position(1);
        buff.mark();

        buff.position(0);

        assertThat(buff::reset, throwing(InvalidMarkException.class));
    }

    @Test
    public void clearMarkWhenLimitLessThanMarkedPosition() throws Throwable {
        buff.position(2);
        buff.mark();

        buff.limit(1);

        assertThat(buff.position(), equalTo(1));
        assertThat(buff::reset, throwing(InvalidMarkException.class));
    }

    @Test
    public void resetRepeatedly() throws Throwable {
        buff.position(1).mark();

        assertThat(buff.position(2).reset().position(), equalTo(1));
        assertThat(buff.position(3).reset().position(), equalTo(1));
    }

    @Test
    public void remaining() throws Throwable {
        buff.limit(1).position(1);

        assertThat(buff.remaining(), equalTo(buff.limit() - buff.position()));
    }

    @Test
    public void clearSetsLimitToCapacityAndPositionToZero() throws Throwable {
        buff.limit(1).position(1);

        buff.clear();

        assertThat(buff.array(), equalTo(new byte[]{1, 2, 3}));
        assertThat(buff.position(), equalTo(0));
        assertThat(buff.limit(), equalTo(buff.capacity()));
    }


    @Test
    public void flipSetsLimitToPositionAndPositionToZero() throws Throwable {
        int last = 1;
        buff.position(last);

        buff.flip();

        assertThat(buff.array(), equalTo(new byte[]{1, 2, 3}));
        assertThat(buff.position(), equalTo(0));
        assertThat(buff.limit(), equalTo(last));
    }


    @Test
    public void rewindSetsPositionToZeroOnly() throws Throwable {
        buff.limit(1).position(1);

        buff.rewind();

        assertThat(buff.position(), equalTo(0));
        assertThat(buff.limit(), equalTo(1));
    }

    @Test
    public void failsToWriteAReadonlyBuffer() throws Throwable {
        ByteBuffer readonly = buff.asReadOnlyBuffer();

        assertThat(() -> readonly.put((byte) -1), throwing(ReadOnlyBufferException.class));
    }

    @Test
    public void readonlyBufferContentCanNotBeChanged() throws Throwable {
        ByteBuffer readonly = buff.asReadOnlyBuffer();

        assertThat(readonly::array, throwing(ReadOnlyBufferException.class));
        assertFalse(readonly.hasArray());
    }

    @Test
    public void get() throws Throwable {
        assertThat(buff.get(), equalTo((byte) 1));

        assertThat(buff.position(), equalTo(1));
    }

    @Test
    public void put() throws Throwable {
        buff.put((byte) 3);
        assertThat(buff.position(), equalTo(1));

        buff.clear();
        assertThat(buff.get(), equalTo((byte) 3));
    }

    @Test
    public void bulkPut() throws Throwable {
        buff.put(new byte[]{1, 2});

        assertThat(buff.position(), equalTo(2));
    }

    @Test
    public void bulkGet() throws Throwable {
        byte[] large = new byte[2];

        buff.get(large);

        assertThat(large, equalTo(new byte[]{1, 2}));
    }

    @Test
    public void failsToBulkGetWhenHasNoEnoughRemaining() throws Throwable {
        byte[] large = new byte[buff.remaining() + 1];

        assertThat(() -> buff.get(large), throwing(BufferUnderflowException.class));
    }

    @Test
    public void failsToBulkPutWhenHasNoEnoughRemaining() throws Throwable {
        byte[] large = new byte[buff.remaining() + 1];

        assertThat(() -> buff.put(large), throwing(BufferOverflowException.class));
    }

    @Test
    public void absoluteGet() throws Throwable {
        assertThat(buff.getShort(), equalTo(toShort(1 << 8 | 2)));
    }

    @Test
    public void order() throws Throwable {
        assertThat(buff.order(BIG_ENDIAN).getShort(), equalTo(toShort(1 << 8 | 2)));

        buff.clear();

        assertThat(buff.order(LITTLE_ENDIAN).getShort(), equalTo(toShort(2 << 8 | 1)));
    }

    @Test
    public void putShort() throws Throwable {
        buff.putShort(1, toShort((3 << 8) | 4));

        assertThat(buff.array(), equalTo(new byte[]{1, 3, 4}));
    }


    @Test
    public void indexGetDonotEffectPosition() throws Throwable {
        assertThat(buff.get(2), equalTo((byte) 3));

        assertThat(buff.position(), equalTo(0));
    }

    private short toShort(int i) {
        return (short) i;
    }

    @Test
    public void compactCopyBytesBetweenPositionInclusiveLimitToHeadAndThenClearPositionLimitAndMark() throws Throwable {
        buff.position(1).mark().limit(2);

        ByteBuffer compacted = buff.compact();

        assertThat(compacted, sameInstance(buff));
        assertThat(compacted.array(), equalTo(new byte[]{2, 2, 3}));
        assertThat(compacted.arrayOffset(), equalTo(0));
        assertThat(compacted::reset, throwing(InvalidMarkException.class));
    }

    @Test
    public void duplicateShareSourceBufferContents() throws Throwable {
        ByteBuffer duplicated = buff.duplicate();

        assertThat(duplicated, is(not(sameInstance(buff))));

        buff.put((byte) 8);
        assertThat(duplicated.get(0), equalTo((byte) 8));
    }

    @Test
    public void duplicatePositionLimitAndMarkAreIndependentAfterSubsequentOperations() throws Throwable {
        buff.position(2).mark();
        ByteBuffer duplicated = buff.duplicate();

        assertThat(buff.position(), equalTo(2));
        assertThat(duplicated.position(), equalTo(2));
        assertThat(buff.limit(), equalTo(3));
        assertThat(duplicated.limit(), equalTo(3));
        buff.reset();
        duplicated.reset();

        buff.clear();
        duplicated.clear().position(1).mark().limit(2);

        assertThat(buff.position(), equalTo(0));
        assertThat(duplicated.position(), equalTo(1));
        assertThat(buff.limit(), equalTo(3));
        assertThat(duplicated.limit(), equalTo(2));
        assertThat(buff::reset, throwing(InvalidMarkException.class));
        duplicated.reset();
    }


    @Test
    public void sliceCreateANewSharedBufferBetweenPositionInclusiveLimitAndMarkIsUndefined() throws Throwable {
        buff.position(1).mark().limit(2);

        ByteBuffer piece = buff.slice();

        assertThat(piece.position(), equalTo(0));
        assertThat(piece.limit(), equalTo(1));
        assertThat(piece.get(), equalTo((byte) 2));

        piece.put(0, (byte) 7);
        assertThat(buff.get(), equalTo((byte) 7));

        buff.reset();
        assertThat(piece::reset, throwing(InvalidMarkException.class));
    }
}
