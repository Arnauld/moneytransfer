package banktransfer.core;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StatusTest {

    @Test
    public void should_return_value_when_ok() {
        Status<AnyThing, String> ok = Status.ok("Hog");
        assertThat(ok.succeeded()).isTrue();
        assertThat(ok.value()).isEqualTo("Hog");
    }

    @Test(expected = IllegalStateException.class)
    public void should_raise_an_error_when_accessing_error_on_ok_case() {
        Status<Failure, String> ok = Status.ok("Hog");
        ok.error();
    }

    @Test
    public void should_return_error_when_error() {
        Status<String, AnyThing> ok = Status.error("Hog");
        assertThat(ok.succeeded()).isFalse();
        assertThat(ok.error()).isEqualTo("Hog");
    }

    @Test(expected = IllegalStateException.class)
    public void should_raise_an_error_when_accessing_value_on_error_case() {
        Status<String, AnyThing> ok = Status.error("Hog");
        ok.value();
    }

    public static class AnyThing {
    }

}