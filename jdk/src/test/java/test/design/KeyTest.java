package test.design;

import org.junit.Test;

import java.util.BitSet;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.matchers.ExceptionMatchers.throwing;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.ThrowableMessageMatcher.hasMessage;

public class KeyTest {

    @Test
    public void stringKeysAreSharedByKey() throws Throwable {
        String stringKey = "I100010";

        assertThat(EncodedKey.from(stringKey).toString(), sameInstance(stringKey));
    }

    @Test
    public void internalKey() throws Throwable {
        PrivateKey key = EncodedKey.from("I100010");

        assertThat(key.type(), equalTo('I'));
        assertThat(key.value(), equalTo("100010"));
        assertThat(key.isExternal(), is(false));
    }

    @Test
    public void externalKey() throws Throwable {
        PrivateKey key = EncodedKey.from("A100010");

        assertThat(key.type(), equalTo('A'));
        assertThat(key.value(), equalTo("100010"));
        assertThat(key.isExternal(), is(true));
    }

    @Test
    public void invalidKey() throws Throwable {
        assertThat(() -> EncodedKey.from("X100010"), throwing(InvalidKeyException.class, hasMessage(equalTo("Invalid key: \"X100010\""))));
    }

    @Test
    public void castAKeyToAPrivateKey() throws Throwable {
        Key key = EncodedKey.from("A100010");

        PrivateKey privateKey = PrivateKey.from(key);

        assertThat(privateKey.isExternal(), is(true));
        assertThat(privateKey.type(), equalTo('A'));
    }

    @Test
    public void failsToCastACustomKeyToPrivateKey() throws Throwable {
        Key key = () -> "custom key";

        assertThat(() -> PrivateKey.from(key), throwing(InvalidKeyException.class));
    }
}

/**
 * Key expose to client
 */
interface Key {
    /**
     * @return the {@code String} key value
     */
    String value();

    /**
     * @return the original {@code String} key
     */
    String toString();
}

/**
 * PrivateKey is used internally
 */
interface PrivateKey extends Key {
    /**
     * @return return the type of a key.
     */
    char type();

    /**
     * @return return <b>true</b> if the key is external, otherwise return <b>false</b>.
     */
    boolean isExternal();

    /**
     * @param key
     * @return a {@link PrivateKey} if it is created by application.
     * @throws InvalidKeyException if the key is created by user
     */
    static PrivateKey from(Key key) throws InvalidKeyException {
        if (key instanceof PrivateKey) return (PrivateKey) key;
        throw new InvalidKeyException(key);
    }
}

class EncodedKey implements PrivateKey {
    private final String key;
    private final Visibility visibility;

    //      v--- make the Key constructor private.
    private EncodedKey(String key, Visibility visibility) {
        this.key = key;
        this.visibility = visibility;
    }

    /**
     * @param key
     * @return create a PrivateKey from a encoded {@code String} key.
     * @throws InvalidKeyException  if the {@code String} key format is invalid
     * @throws NullPointerException if the {@code String} key is null
     */
    public static PrivateKey from(String key) throws InvalidKeyException {
        return new EncodedKey(key, visibilityOf(key));
    }

    private static final BitSet externals = new BitSet();

    static {
        externals.set('A');
        externals.set('B');
    }

    private static Visibility visibilityOf(String key) throws InvalidKeyException {
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
        throw new InvalidKeyException(key);
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

class InvalidKeyException extends IllegalArgumentException {
    public InvalidKeyException(Key key) {
        this(key.toString());
    }

    public InvalidKeyException(String key) {
        super("Invalid key: \"" + key + "\"");
    }
}