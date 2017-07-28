package test.design;

import org.junit.Test;

import java.util.BitSet;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.matchers.ExceptionMatchers.throwing;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.ThrowableMessageMatcher.hasMessage;

public class KeyTest {

    @Test
    public void stringKeysAreSharedByKey() throws Throwable {
        String stringKey = "I100010";

        assertThat(Key.from(stringKey).toString(), sameInstance(stringKey));
    }

    @Test
    public void internalKey() throws Throwable {
        Key key = Key.from("I100010");

        assertThat(key.type(), equalTo('I'));
        assertThat(key.value(), equalTo("100010"));
        assertThat(key.isExternal(), is(false));
    }

    @Test
    public void externalKey() throws Throwable {
        Key key = Key.from("A100010");

        assertThat(key.type(), equalTo('A'));
        assertThat(key.value(), equalTo("100010"));
        assertThat(key.isExternal(), is(true));
    }

    @Test
    public void invalidKey() throws Throwable {
        assertThat(() -> Key.from("X100010"), throwing(IllegalArgumentException.class, hasMessage(equalTo("Invalid Key: \"X100010\""))));
    }
}


class Key {
    private final String key;
    private final Visibility visibility;

    //      v--- make the Key constructor private.
    private Key(String key, Visibility visibility) {
        this.key = key;
        this.visibility = visibility;
    }


    public static Key from(String key) {
        return new Key(key, visibilityOf(key));
    }

    private static final BitSet externals = new BitSet();

    static {
        externals.set('A');
        externals.set('B');
    }

    private static Visibility visibilityOf(String key) {
        //@formatter:off
        return isInternalKey(key) ? Visibility.INTERNAL
                                  : isExternalKey(key) ? Visibility.EXTERNAL
                                                       : failsOnInvalidKey(key);
        //@formatter:on
    }

    private static boolean isInternalKey(String key) {
        return key.charAt(0) == 'I';
    }

    private static boolean isExternalKey(String key) {
        return externals.get(key.charAt(0));
    }

    private static Visibility failsOnInvalidKey(String key) {
        throw new IllegalArgumentException("Invalid Key: \"" + key + "\"");
    }

    public char type() {
        return key.charAt(0);
    }

    public String value() {
        return key.substring(1);
    }

    public boolean isExternal() {
        return visibility == Visibility.EXTERNAL;
    }

    public String toString() {
        return key;
    }

    // preserve it if will introduce additional behavior in future
    private enum Visibility {
        EXTERNAL,
        INTERNAL
    }
}

