package kotlin;

import org.junit.Test;
import params.Param;
import params.ParamsKt;

import java.lang.reflect.Method;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class PublishedApiTest {
    @Test
    public void canNotBeAccessSyntheticPublishedApiDirectly() throws Throwable {
        @SuppressWarnings("JavaReflectionMemberAccess")
        Method published = ParamsKt.class.getDeclaredMethod("singleton", Object.class);

        @SuppressWarnings("unchecked")
        Param<String> param = (Param<String>) published.invoke(null, "foo");

        assertThat(param.map(it -> it), equalTo("foo"));
    }
}
