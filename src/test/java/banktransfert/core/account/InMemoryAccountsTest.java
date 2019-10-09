package banktransfert.core.account;

import banktransfert.core.Email;
import banktransfert.core.Failure;
import banktransfert.core.Status;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class InMemoryAccountsTest {

    private static final AccountId ACCOUNT_ID1 = AccountId.accountId("w0001").value();
    private static final AccountId ACCOUNT_ID2 = AccountId.accountId("w0002").value();

    private Accounts accounts;
    private AccountIdGenerator accountIdGenerator;

    @Before
    public void setUp() {
        accountIdGenerator = Mockito.mock(AccountIdGenerator.class);
        accounts = new InMemoryAccounts(accountIdGenerator);
    }

    @Test
    public void should_create_new_account() {
        when(accountIdGenerator.newAccountId()).thenReturn(ACCOUNT_ID1);

        NewAccount newAccount = NewAccount.newAccount(Email.email("titania@tyrna.nog"), "Titania").value();
        Status<Failure, AccountId> accountIdStatus = accounts.add(newAccount);
        assertThat(accountIdStatus.succeeded()).isTrue();
        assertThat(accountIdStatus.value().asString()).isNotBlank();
    }

    @Test
    public void should_generate_different_id_when_creating_multiple_accounts() {
        when(accountIdGenerator.newAccountId()).thenReturn(ACCOUNT_ID1, ACCOUNT_ID2);

        NewAccount newAccount1 = NewAccount.newAccount(Email.email("titania@tyrna.nog"), "Titania").value();
        NewAccount newAccount2 = NewAccount.newAccount(Email.email("oberon@tyrna.nog"), "Oberon").value();
        Status<Failure, AccountId> accountIdStatus1 = accounts.add(newAccount1);
        Status<Failure, AccountId> accountIdStatus2 = accounts.add(newAccount2);
        assertThat(accountIdStatus1.value()).isNotEqualTo(accountIdStatus2.value());
        assertThat(accountIdStatus1.value()).isEqualTo(ACCOUNT_ID1);
        assertThat(accountIdStatus2.value()).isEqualTo(ACCOUNT_ID2);
    }

    @Test
    public void should_not_allow_to_create_two_accounts_with_same_email() {
        when(accountIdGenerator.newAccountId()).thenReturn(ACCOUNT_ID1, ACCOUNT_ID2);

        NewAccount newAccount = NewAccount.newAccount(Email.email("titania@tyrna.nog"), "Titania").value();
        Status<Failure, AccountId> accountIdStatus1 = accounts.add(newAccount);
        Status<Failure, AccountId> accountIdStatus2 = accounts.add(newAccount);
        assertThat(accountIdStatus1.succeeded()).isTrue();
        assertThat(accountIdStatus2.succeeded()).isFalse();
        assertThat(accountIdStatus2.error().error()).isEqualTo("email-already-inuse");
    }

}