package banktransfer.core;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EmailTest {

    @Test
    public void should_reject_null_or_empty_argument() {
        assertThat(Email.email(null).succeeded()).isFalse();
        assertThat(Email.email(null).error().error()).isEqualTo("no-email-provided");
        assertThat(Email.email("").succeeded()).isFalse();
        assertThat(Email.email("").error().error()).isEqualTo("no-email-provided");
        assertThat(Email.email("  ").succeeded()).isFalse();
        assertThat(Email.email("  ").error().error()).isEqualTo("no-email-provided");
    }

    @Test
    public void should_accept_valid_email() {
        assertThat(Email.email("a@a.a").succeeded()).isTrue();
        assertThat(Email.email("a@a.a").value().asString()).isEqualTo("a@a.a");
    }

    @Test
    public void should_lower_case_valid_email() {
        assertThat(Email.email("A@a.a").value().asString()).isEqualTo("a@a.a");
    }

    @Test
    public void should_reject_invalid_emai() {
        assertThat(Email.email("a@a").succeeded()).isFalse();
        assertThat(Email.email("a@a.").succeeded()).isFalse();
        assertThat(Email.email("a@a.@").succeeded()).isFalse();
        assertThat(Email.email("a@a.@").error().error()).isEqualTo("invalid-email");
    }

    @Test
    public void should_ensure_consistency_with_equals_and_hashcode() {
        assertThat(Email.email("a@a.hog").value()).isEqualTo(Email.email("a@a.hog").value());
        assertThat(Email.email("a@a.hog").value()).isNotEqualTo(Email.email("b@a.hog").value());
        assertThat(Email.email("b@a.hog").value()).isEqualTo(Email.email("b@a.hog").value());
        assertThat(Email.email("a@a.hog").value().hashCode()).isEqualTo(Email.email("a@a.hog").value().hashCode());


    }
}