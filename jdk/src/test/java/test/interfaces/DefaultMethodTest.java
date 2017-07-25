package test.interfaces;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class DefaultMethodTest {
    interface DefaultMethod {
        default String foo() {
            return "bar";
        }
    }

    @Test
    public void accessDefaultMethodByReflection() throws Throwable {
        //@formatter:off
        DefaultMethod it = new DefaultMethod() {/**/};
        //@formatter:on

        assertThat(invokeDefaultMethod(it), equalTo("bar"));
    }

    @Test
    public void defaultMethodsWillBeOverride() throws Throwable {
        DefaultMethod it = new DefaultMethod() {
            public String foo() {
                return "baz";
            }
        };

        assertThat(invokeDefaultMethod(it), equalTo("baz"));
    }

    private String invokeDefaultMethod(DefaultMethod it) throws Throwable {
        return (String) DefaultMethod.class.getMethod("foo").invoke(it);
    }
}
