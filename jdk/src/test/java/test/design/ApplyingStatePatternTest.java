package test.design;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.matchers.ExceptionMatchers.throwing;
import static org.junit.Assert.assertThat;
import static test.design.Optional.absent;

@RunWith(Enclosed.class)
public class ApplyingStatePatternTest {
    public static class WhenAbsent {
        private final Optional<String> absent = absent();

        @Test
        public void throwsNullPointerExceptionWhenGetFromAbsentValue() {
            assertThat(absent::get, throwing(NullPointerException.class));
        }

        @Test
        public void isAbsent() {
            assertThat(absent.isPresent(), is(false));
        }

        @Test
        public void alwaysAbsentWhenFiltering() {
            assertThat(absent.filter(String::isEmpty), sameInstance(absent));
        }

        @Test
        public void alwaysAbsentWhenMapping() {
            assertThat(absent.map(String::isEmpty), sameInstance(absent));
        }

        @Test
        public void alwaysAbsentWhenFlatMapping() {
            assertThat(absent.flatMap(it -> Optional.present("foo")), sameInstance(absent));
        }

        @Test
        public void alwaysReturnTheDefaultValue() {
            assertThat(absent.orElse("default"), equalTo("default"));
            assertThat(absent.orElseGet(() -> "default"), equalTo("default"));
        }

        @Test
        public void alwaysThrowsTheProvidedException() {
            assertThat(() -> absent.orElseThrow(IllegalStateException::new), throwing(IllegalStateException.class));
        }

        @Test
        public void neverCallingTheProvidedAction() {
            List<String> received = new ArrayList<>();

            absent.ifPresent(received::add);

            assertThat(received, is(empty()));
        }
    }

    public static class WhenPresent {
        private final Optional<String> present = Optional.present("foo");

        @Test
        public void getsTheRawValue() {
            assertThat(present.get(), equalTo("foo"));
        }

        @Test
        public void isPresent() {
            assertThat(present.isPresent(), is(true));
        }

        @Test
        public void throwsNullPointerExceptionIfCreateAPresentOptionalWithNullValue() {
            //noinspection ResultOfMethodCallIgnored
            assertThat(() -> Optional.present(null), throwing(NullPointerException.class));
        }

        @Test
        public void stayOnPresentIfFilteringIsSatisfied() {
            assertThat(present.filter("FOO"::equalsIgnoreCase), equalTo(present));
        }

        @Test
        public void transitionToAbsentIfFilteringIsUnsatisfied() {
            assertThat(present.filter("FOO"::equals), sameInstance(absent()));
        }

        @Test
        public void stayOnPresentWhenMappedValueIsPresent() {
            assertThat(present.map(String::isEmpty).get(), is(false));
        }

        @Test
        public void transitionToAbsentWhenMappedValueIsAbsent() {
            assertThat(present.map(ignored -> null), sameInstance(absent()));
        }

        @Test
        public void transitionToTheFlatMappedOptional() {
            assertThat(present.flatMap(unused -> absent()), sameInstance(absent()));
        }

        @Test
        public void throwsNullPointerExceptionWhenTheFlatMappedOptionalIsNull() {
            assertThat(() -> present.flatMap(unused -> null), throwing(NullPointerException.class));
        }

        @Test
        public void alwaysReturnTheRawValue() {
            assertThat(present.orElse("default"), equalTo("foo"));
            assertThat(present.orElseGet(() -> "default"), equalTo("foo"));
            assertThat(present.orElseThrow(IllegalStateException::new), equalTo("foo"));
        }

        @Test
        public void alwaysCallingTheProvidedActionWithTheRawValue() {
            List<String> received = new ArrayList<>();

            present.ifPresent(received::add);

            assertThat(received, equalTo(singletonList("foo")));
        }
    }


}

interface Optional<T> {
    T get();

    boolean isPresent();

    Optional<T> filter(Predicate<T> predicate);

    <R> Optional<R> map(Function<T, R> mapping);

    <R> Optional<R> flatMap(Function<T, Optional<R>> mapping);

    T orElse(T other);

    T orElseGet(Supplier<T> other);

    void ifPresent(Consumer<T> action);

    <E extends Throwable> T orElseThrow(Supplier<? extends E> exceptional) throws E;

    Optional<?> ABSENT = new Optional<Object>() {
        @Override
        public Object get() {
            throw new NullPointerException();
        }

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public Optional<Object> filter(Predicate<Object> predicate) {
            return this;
        }

        @Override
        public <R> Optional<R> map(Function<Object, R> mapping) {
            return absent();
        }

        @Override
        public <R> Optional<R> flatMap(Function<Object, Optional<R>> mapping) {
            return absent();
        }

        @Override
        public Object orElse(Object other) {
            return other;
        }

        @Override
        public Object orElseGet(Supplier<Object> other) {
            return other.get();
        }

        @Override
        public void ifPresent(Consumer<Object> action) {

        }

        @Override
        public <E extends Throwable> Object orElseThrow(Supplier<? extends E> exceptional) throws E {
            throw exceptional.get();
        }
    };

    static <T> Optional<T> of(T result) {
        return result == null ? absent() : present(result);
    }

    @SuppressWarnings("unchecked")
    static <T> Optional<T> absent() {
        return (Optional<T>) ABSENT;
    }

    static <T> Optional<T> present(T value) {
        requireNonNull(value);
        return new Optional<T>() {
            @Override
            public T get() {
                return value;
            }

            @Override
            public boolean isPresent() {
                return true;
            }

            @Override
            public Optional<T> filter(Predicate<T> predicate) {
                return predicate.test(value) ? this : absent();
            }

            @Override
            public <R> Optional<R> map(Function<T, R> mapping) {
                return of(mapping.apply(value));
            }

            @Override
            public <R> Optional<R> flatMap(Function<T, Optional<R>> mapping) {
                return requireNonNull(mapping.apply(value));
            }

            @Override
            public T orElse(T other) {
                return value;
            }

            @Override
            public T orElseGet(Supplier<T> other) {
                return value;
            }

            @Override
            public void ifPresent(Consumer<T> action) {
                action.accept(value);
            }

            @Override
            public <E extends Throwable> T orElseThrow(Supplier<? extends E> exceptional) throws E {
                return value;
            }
        };
    }

}