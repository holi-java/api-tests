package org.hamcrest.matchers;

import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.jetbrains.annotations.NotNull;

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
        return new FeatureMatcher<ExceptionExpectation, Throwable>(allOf(matchers), "throwing", "") {
            @Override
            protected Throwable featureValueOf(ExceptionExpectation actual) {
                try {
                    actual.run();
                } catch (Throwable e) {
                    return e;
                }
                throw new AssertionError(descriptionOfNoExceptionThrowing());
            }

            @NotNull
            private Description descriptionOfNoExceptionThrowing() {
                return new StringDescription().appendText("\nExpected: ").appendDescriptionOf(this)
                        .appendText("\nbut: No Exception was thrown!");
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
