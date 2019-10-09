package banktransfert.core.account;

import banktransfert.core.Email;
import banktransfert.core.Failure;
import banktransfert.core.Status;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InMemoryAccountsTest {

    private Accounts accounts = new InMemoryAccounts();

    @Test
    public void should_create_new_account() {
        NewAccount newAccount = NewAccount.newAccount(Email.email("titania@tyrna.nog"), "Titania").value();
        Status<Failure, AccountId> accountIdStatus = accounts.create(newAccount);
        assertThat(accountIdStatus.succeeded()).isTrue();
        assertThat(accountIdStatus.value().asString()).isNotBlank();
    }

    @Test
    public void should_generate_different_id_when_creating_multiple_accounts() {
        NewAccount newAccount1 = NewAccount.newAccount(Email.email("titania@tyrna.nog"), "Titania").value();
        NewAccount newAccount2 = NewAccount.newAccount(Email.email("oberon@tyrna.nog"), "Oberon").value();
        Status<Failure, AccountId> accountIdStatus1 = accounts.create(newAccount1);
        Status<Failure, AccountId> accountIdStatus2 = accounts.create(newAccount2);
        assertThat(accountIdStatus1.value()).isNotEqualTo(accountIdStatus2.value());
    }

}