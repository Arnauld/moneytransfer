package banktransfert;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MainTest {

    @Test
    public void public_constants_should_remain_unchanged() {
        assertThat(Main.HTTP_PORT).isEqualTo("http.port");
        assertThat(Main.HTTP_INSTANCES).isEqualTo("http.instances");
    }
}