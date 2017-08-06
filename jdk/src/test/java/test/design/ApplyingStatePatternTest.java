package test.design;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.matchers.ExceptionMatchers.throwing;
import static org.junit.Assert.assertThat;
import static test.design.Optional.*;
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
            assertThat(absent.flatMap(it -> present("foo")), sameInstance(absent));
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
        private final Optional<String> present = present("foo");

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
            assertThat(() -> present(null), throwing(NullPointerException.class));
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

    public static class EqualityTest {
        @Test
        public void equality() throws Throwable {
            assertThat(absent(), equalTo(absent()));
            assertThat(present("foo"), equalTo(present("foo")));
            assertThat(present("foo"), not(equalTo(absent())));
            assertThat(present("foo"), not(equalTo("foo")));
        }

        @Test
        public void hash() throws Throwable {
            assertThat(absent().hashCode(), equalTo(0));
            assertThat(present("foo").hashCode(), equalTo("foo".hashCode()));
        }
    }
}

@SuppressWarnings("WeakerAccess")
abstract class Optional<T> {

    private static final Optional<?> ABSENT = new Optional<Object>() {
        @Override
        public Optional<Object> filter(Predicate<? super Object> predicate) {
            return this;
        }

        @Override
        public <R> Optional<R> map(Function<? super Object, ? extends R> mapping) {
            return absent();
        }

        @Override
        public <R> Optional<R> flatMap(Function<? super Object, ? extends Optional<R>> mapping) {
            return absent();
        }

        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            return "absent";
        }
    };

    private Optional() {/*sealed class*/}

    public static <T> Optional<T> of(T result) {
        return result == null ? absent() : present(result);
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> absent() {
        return (Optional<T>) ABSENT;
    }

    public static <T> Optional<T> present(T value) {
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
            public Optional<T> filter(Predicate<? super T> predicate) {
                return predicate.test(value) ? this : absent();
            }

            @Override
            public <R> Optional<R> map(Function<? super T, ? extends R> mapping) {
                return of(mapping.apply(value));
            }

            @Override
            public <R> Optional<R> flatMap(Function<? super T, ? extends Optional<R>> mapping) {
                return requireNonNull(mapping.apply(value));
            }

            @Override
            public T orElse(T other) {
                return value;
            }

            @Override
            public T orElseGet(Supplier<? extends T> other) {
                return value;
            }

            @Override
            public void ifPresent(Consumer<? super T> action) {
                action.accept(value);
            }

            @Override
            public <E extends Throwable> T orElseThrow(Supplier<? extends E> exceptional) throws E {
                return value;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) return true;
                if (!getClass().isInstance(obj)) return false;
                Optional<?> that = (Optional<?>) obj;
                return value.equals(that.get());
            }

            @Override
            public int hashCode() {
                return value.hashCode();
            }

            @Override
            public String toString() {
                return "`" + value + "`";
            }
        };
    }

    public abstract Optional<T> filter(Predicate<? super T> predicate);

    public abstract <R> Optional<R> map(Function<? super T, ? extends R> mapping);

    public abstract <R> Optional<R> flatMap(Function<? super T, ? extends Optional<R>> mapping);

    public T get() {
        throw new NullPointerException();
    }

    public boolean isPresent() {
        return false;
    }

    public T orElse(T other) {
        return other;
    }

    public T orElseGet(Supplier<? extends T> other) {
        return other.get();
    }

    public void ifPresent(Consumer<? super T> action) {/*ignore the action by default*/}

    public <E extends Throwable> T orElseThrow(Supplier<? extends E> exceptional) throws E {
        throw exceptional.get();
    }

}