package banktransfert.core.account;

import org.junit.Test;

import static banktransfert.core.account.AccountId.accountId;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountIdTest {

    @Test(expected = IllegalArgumentException.class)
    public void should_reject_null_argument() {
        accountId(null);
    }

    @Test
    public void should_ensure_consistency_with_equals_and_hashcode() {
        assertThat(accountId("1234")).isEqualTo(accountId("1234"));
        assertThat(accountId("azer")).isEqualTo(accountId("azer"));
        assertThat(accountId("1234").hashCode()).isEqualTo(accountId("1234").hashCode());
        assertThat(accountId("azer").hashCode()).isEqualTo(accountId("azer").hashCode());
        //
        assertThat(accountId("azer")).isNotEqualTo(accountId("Azer"));
        assertThat(accountId("azer")).isNotEqualTo(accountId("az3r"));
        assertThat(accountId("azer")).isNotEqualTo(accountId("az3r"));
    }

    @Test
    public void should_return_raw_value() {
        assertThat(accountId("aefc34").asString()).isEqualTo("aefc34");
    }

}