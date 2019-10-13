package banktransfert.core.account.inmemory;

import banktransfert.core.Email;
import banktransfert.core.Failure;
import banktransfert.core.Status;
import banktransfert.core.account.AccountId;
import banktransfert.core.account.AccountIdGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static banktransfert.core.Status.ok;
import static banktransfert.core.account.NewAccount.newAccount;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class InMemoryAccountsTest {

    private static final BigDecimal M_250 = BigDecimal.valueOf(250);
    private static final AccountId ACCOUNT_ID1 = AccountId.accountId("w001").value();
    private static final AccountId ACCOUNT_ID2 = AccountId.accountId("w002").value();

    private AccountIdGenerator accountIdGenerator;
    private InMemoryAccounts accounts;

    @Before
    public void setUp() {
        accountIdGenerator = Mockito.mock(AccountIdGenerator.class);
        accounts = new InMemoryAccounts(accountIdGenerator);
    }

    @Test
    public void should_add_new_account_and_generate_new_id() {
        when(accountIdGenerator.newAccountId()).thenReturn(ACCOUNT_ID1);
        Status<Failure, AccountId> status = accounts.add(newAccount(Email.email("titania@tyrna.nog"), ok(M_250)).value());

        assertThat(status.succeeded()).isTrue();
        assertThat(status.value()).isNotNull().isEqualTo(ACCOUNT_ID1);
    }

    @Test
    public void should_not_add_new_account_when_same_email_is_already_present() {
        when(accountIdGenerator.newAccountId()).thenReturn(ACCOUNT_ID1);
        Status<Failure, AccountId> status1 = accounts.add(newAccount(Email.email("titania@tyrna.nog"), ok(M_250)).value());
        Status<Failure, AccountId> status2 = accounts.add(newAccount(Email.email("titania@tyrna.nog"), ok(M_250)).value());

        assertThat(status1.succeeded()).isTrue();
        assertThat(status1.value()).isNotNull().isEqualTo(ACCOUNT_ID1);
        assertThat(status2.succeeded()).isFalse();
        assertThat(status2.error().error()).isNotNull().isEqualTo("email-already-inuse");
    }

}