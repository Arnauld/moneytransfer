package banktransfert.core.account;

import org.junit.Test;

import static banktransfert.core.account.AccountId.accountId;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountIdTest {

    @Test
    public void should_reject_null_argument() {
        assertThat(accountId(null).succeeded()).isFalse();
    }

    @Test
    public void should_ensure_consistency_with_equals_and_hashcode() {
        assertThat(accountId("1234").value()).isEqualTo(accountId("1234").value());
        assertThat(accountId("azer").value()).isEqualTo(accountId("azer").value());
        assertThat(accountId("1234").value().hashCode()).isEqualTo(accountId("1234").value().hashCode());
        assertThat(accountId("azer").value().hashCode()).isEqualTo(accountId("azer").value().hashCode());
        //
        assertThat(accountId("azer").value()).isNotEqualTo(accountId("Azer").value());
        assertThat(accountId("azer").value()).isNotEqualTo(accountId("az3r").value());
        assertThat(accountId("azer").value()).isNotEqualTo(accountId("az3r").value());
    }

    @Test
    public void should_return_raw_value() {
        assertThat(accountId("aefc34").value().asString()).isEqualTo("aefc34");
    }

}