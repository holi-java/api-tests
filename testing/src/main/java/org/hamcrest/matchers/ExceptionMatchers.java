package org.hamcrest.matchers;

import org.hamcrest.*;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;

public class ExceptionMatchers {
    public interface ExceptionExpectation {
        void run() throws Throwable;
    }

    public static Matcher<ExceptionExpectation> throwing(Class<? extends Throwable> expected) {
        return throwing(singleton(instanceOf(expected)));
    }

    @SafeVarargs
    public static Matcher<ExceptionExpectation> throwing(Class<? extends Throwable> expected, Matcher<? super Throwable>... matchers) {
        return throwing(concat(singleton(instanceOf(expected)), asList(matchers)));
    }

    private static Collection<Matcher<? super Throwable>> concat(Collection<Matcher<? super Throwable>> left, Collection<Matcher<? super Throwable>> right) {
        return Stream.of(left, right).flatMap(Collection::stream).collect(toList());
    }


    private static Matcher<ExceptionExpectation> throwing(Collection<Matcher<? super Throwable>> matchers) {
        return new TypeSafeDiagnosingMatcher<ExceptionExpectation>() {
            Matcher<Throwable> exceptionMatcher = allOf(matchers);

            @Override
            protected boolean matchesSafely(ExceptionExpectation item, Description mismatch) {
                try {
                    item.run();
                    mismatch.appendText("No exception is thrown!");
                    return false;
                } catch (Throwable ex) {
                    return matchesThrownException(ex, mismatch);
                }
            }

            private boolean matchesThrownException(Throwable ex, Description mismatch) {
                if (!exceptionMatcher.matches(ex)) {
                    mismatch.appendValue(ex);
                    return false;
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("throwing ").appendDescriptionOf(exceptionMatcher);
            }
        };
    }


    public static Matcher<Throwable> withMessage(String message) {
        return withMessage(equalTo(message));
    }

    public static Matcher<Throwable> withMessage(Matcher<String> matcher) {
        return hasProperty("message", matcher);
    }
}
