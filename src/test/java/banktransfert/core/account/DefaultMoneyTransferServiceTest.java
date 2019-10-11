package banktransfert.core.account;

import banktransfert.core.account.inmemory.InMemoryAccounts;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static banktransfert.core.Email.email;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class DefaultMoneyTransferServiceTest {

    private static final TransactionId TRANSACTION_ID = TransactionId.transactionId("w001").value();
    private static final BigDecimal M_750 = BigDecimal.valueOf(750);
    private static final BigDecimal M_500 = BigDecimal.valueOf(500);
    private static final BigDecimal M_250 = BigDecimal.valueOf(250);

    private MoneyTransferService moneyTransferService;
    //
    private Accounts accounts;
    private AccountId accountId1;
    private AccountId accountId2;

    @Before
    public void setUp() {
        accounts = new InMemoryAccounts(new SequenceAccountIdGenerator());
        moneyTransferService = new DefaultMoneyTransferService(accounts);

        accountId1 = newAccount("titania@tyna.nog", M_500);
        accountId2 = newAccount("oberon@tyna.nog", M_500);
    }

    private AccountId newAccount(String email, BigDecimal initialAmount) {
        return accounts.add(NewAccount.newAccount(email(email), initialAmount).value()).value();
    }

    @Test
    public void should_not_debit_directly_but_wait_for_tranactions_processing() {
        MoneyTransfer moneyTransfer = new MoneyTransfer(TRANSACTION_ID, accountId1, accountId2, M_250);

        moneyTransferService.transfer(moneyTransfer);

        Account account1 = accounts.findById(accountId1).get();
        Account account2 = accounts.findById(accountId2).get();
        assertThat(account1.balance()).describedAs("Balance is unchanged").isEqualTo(M_500);
        assertThat(account2.balance()).describedAs("Balance is unchanged").isEqualTo(M_500);
        assertThat(account1.transactions()).hasSize(1);
        assertThat(account2.transactions()).hasSize(0);
    }

    @Test
    public void should_debit_amount_from_origin_and_then_propagate_to_destination() {
        MoneyTransfer moneyTransfer = new MoneyTransfer(TRANSACTION_ID, accountId1, accountId2, M_250);

        moneyTransferService.transfer(moneyTransfer);
        moneyTransferService.propagateTransactions();
        // since account may be processed in any order
        // account2 may have been processed before the incoming transaction has been transmitted
        // to be sure one call 'propagateTransactions' a second time
        moneyTransferService.propagateTransactions();

        Account account1 = accounts.findById(accountId1).get();
        Account account2 = accounts.findById(accountId2).get();
        assertThat(account1.balance()).describedAs("Balance has been updated").isEqualTo(M_250);
        assertThat(account2.balance()).describedAs("Balance is unchanged").isEqualTo(M_750);
        assertThat(account1.transactions()).hasSize(1);
        assertThat(account2.transactions()).hasSize(1);
    }


}
