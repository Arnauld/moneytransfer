package banktransfer.core;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FailureTest {

    @Test
    public void should_return_error_as_is() {
        assertThat(new Failure("ooops").error()).isEqualTo("ooops");
    }

}