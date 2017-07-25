package kotlin;

import org.junit.Test;
import params.Param;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class PublishedApiTest {
    @Test
    public void reportsWarningsByIDEAOnlyWhenAccessPublishedApiButCanBeSuppressed() throws Throwable {
        @SuppressWarnings("KotlinInternalInJava")
        Param<String> param = new params.Singleton<>("foo");

        assertThat(param.map(it -> it), equalTo("foo"));
    }
}
