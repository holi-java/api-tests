package test.design;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static test.design.Builder.build;
import static test.design.MapBuilder.hashMap;

public class ApplyBuilderDesignPatternTest {

    @Test
    public void buildMap() throws Throwable {
        Map<String, String> it = build(hashMap(), that -> that.with("foo", "bar").with("key", "value"));

        assertThat(it, equalTo(new HashMap<String, String>() {{
            put("foo", "bar");
            put("key", "value");
        }}));
    }

    @Test
    public void breakBuildingSteps() throws Throwable {
        Initializer<MapBuilder<Integer, String>> one = that -> that.with(1, "one");
        Initializer<MapBuilder<Integer, String>> two = that -> that.with(2, "two");

        Map<Integer, String> it = build(hashMap(), one.and(two));

        assertThat(it, equalTo(new HashMap<Integer, String>() {{
            put(1, "one");
            put(2, "two");
        }}));
    }


}

interface Initializer<T> extends Consumer<T> {

    default Initializer<T> and(Initializer<? super T> after) {
        return andThen(after)::accept;
    }
}

interface Builder<T> {
    static <B extends Builder<V>, V> V build(B builder, Initializer<? super B> action) {
        action.accept(builder);
        return builder.build();
    }

    T build();
}

interface MapBuilder<K, V> extends Builder<Map<K, V>> {
    static <K, V> MapBuilder<K, V> hashMap() {
        return mapOf(HashMap::new);
    }

    static <K, V> MapBuilder<K, V> mapOf(final Supplier<Map<K, V>> generator) {
        return new MapBuilder<K, V>() {
            private final Map<K, V> map = generator.get();

            @Override
            public MapBuilder<K, V> with(K key, V value) {
                map.put(key, value);
                return this;
            }

            @Override
            public Map<K, V> build() {
                return map;
            }
        };
    }

    MapBuilder<K, V> with(K key, V value);
}