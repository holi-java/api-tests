package test.nio;

import org.junit.Before;
import org.junit.Test;

import java.nio.*;

import static java.nio.ByteBuffer.*;
import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.matchers.ExceptionMatchers.throwing;
import static org.junit.Assert.*;

public class ViewBufferTest {
    private final ByteBuffer buff = allocate(7);
    private final ShortBuffer view = buff.asShortBuffer();

    @Before
    public void fill() {
        buff.put(new byte[]{1, 2, 3, 4, 5, 6, 7}).clear();
    }


    @Test
    public void startingValues() throws Throwable {
        buff.position(buff.capacity() - 1/*last*/);

        ShortBuffer view = buff.asShortBuffer();

        assertThat(view.capacity(), equalTo(0));
        assertThat(view.remaining(), equalTo(buff.remaining() >> 1));
        assertThat(view.capacity(), equalTo(buff.remaining() >> 1));
    }

    @Test
    public void get() throws Throwable {
        assertThat(view.get(), equalTo(toShort(1 << 8 | 2)));
        assertThat(view.remaining(), equalTo(view.capacity() - 1));
    }

    @Test
    public void contentsBackedBySourceBuffer() throws Throwable {
        byte changes = 5;

        buff.put(changes);

        assertThat(view.get(), equalTo(toShort(changes << 8 | 2)));
    }


    @Test
    public void positionLimitAndMarkAreIndependentWithSourceBuffer() throws Throwable {
        buff.mark();

        assertThat(view::reset, throwing(InvalidMarkException.class));
        buff.reset();
    }

    @Test
    public void indexedInTermsOfTypeSpecificSize() throws Throwable {
        assertThat(view.get(1), equalTo(toShort(3 << 8 | 4)));
        assertThat(view.position(), equalTo(0));
    }


    @Test
    public void relativeBulkGet() throws Throwable {
        short[] dst = new short[2];

        view.get(dst);

        assertThat(dst, equalTo(new short[]{1 << 8 | 2, 3 << 8 | 4}));
    }

    @Test
    public void directDependsOnSourceBuffer() throws Throwable {
        assertFalse(allocate(3).asShortBuffer().isDirect());
        assertTrue(allocateDirect(3).asShortBuffer().isDirect());
    }

    private short toShort(int i) {
        return (short) i;
    }
}
