package banktransfert.core.account.inmemory;

import banktransfert.core.account.AccountId;
import banktransfert.core.Failure;
import banktransfert.core.Status;
import banktransfert.core.account.MoneyTransfer;
import banktransfert.core.account.MoneyTransferService;
import banktransfert.core.account.Transaction;
import banktransfert.core.account.TransactionId;
import banktransfert.core.account.TransactionStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class InMemoryAccountTest {
    private static final TransactionId TRANSACTION_ID = TransactionId.transactionId("t0001").value();
    private static final AccountId ACCOUNT_ID1 = AccountId.accountId("a0001").value();
    private static final AccountId ACCOUNT_ID2 = AccountId.accountId("a0002").value();
    private static final BigDecimal M_100 = BigDecimal.valueOf(100);
    private static final BigDecimal M_150 = BigDecimal.valueOf(150);
    private static final BigDecimal M_350 = BigDecimal.valueOf(350);

    private InMemoryAccount account;

    @Before
    public void setUp() {
        account = new InMemoryAccount(ACCOUNT_ID1, M_350, emptyList());
    }

    @Test
    public void should_provide_state_access() {
        assertThat(account.accountId()).isEqualTo(ACCOUNT_ID1);
        assertThat(account.balance()).isEqualTo(M_350);
        assertThat(account.transactions()).hasSize(0);
    }

    @Test
    public void should_generate_transaction_on_withdraw_but_keep_balance_unchanged() {
        //
        account.withdraw(new MoneyTransfer(TRANSACTION_ID, ACCOUNT_ID1, ACCOUNT_ID2, M_100));
        //
        assertThat(account.balance()).isEqualTo(M_350);
        assertThat(account.transactions()).hasSize(1);
        Transaction tx = account.transactions().findFirst().get();
        assertThat(tx.transactionId()).isEqualTo(TRANSACTION_ID);
        assertThat(tx.status()).isEqualTo(TransactionStatus.Pending);
    }

    @Test
    public void should_generate_transaction_on_credit_but_keep_balance_unchanged() {
        //
        account.credit(new MoneyTransfer(TRANSACTION_ID, ACCOUNT_ID1, ACCOUNT_ID2, M_100));
        //
        assertThat(account.balance()).isEqualTo(M_350);
        assertThat(account.transactions()).hasSize(1);
        Transaction tx = account.transactions().findFirst().get();
        assertThat(tx.transactionId()).isEqualTo(TRANSACTION_ID);
        assertThat(tx.status()).isEqualTo(TransactionStatus.Pending);
    }

    @Test
    public void should_withdraw_only_when_source_account() {
        //
        MoneyTransfer moneyTransfer = new MoneyTransfer(TRANSACTION_ID, ACCOUNT_ID2, ACCOUNT_ID1, M_100);
        //
        Status<Failure, TransactionId> withdraw = account.withdraw(moneyTransfer);
        //
        assertThat(withdraw.succeeded()).isFalse();
        assertThat(withdraw.error().error()).isEqualTo("not-source-account");
    }

    @Test
    public void should_not_withdraw_twice_transfer_with_same_transaction_id() {
        //
        MoneyTransfer moneyTransfer1 = new MoneyTransfer(TRANSACTION_ID, ACCOUNT_ID1, ACCOUNT_ID2, M_100);
        MoneyTransfer moneyTransfer2 = new MoneyTransfer(TRANSACTION_ID, ACCOUNT_ID1, ACCOUNT_ID2, M_150);
        //
        Status<Failure, TransactionId> withdraw1 = account.withdraw(moneyTransfer1);
        Status<Failure, TransactionId> withdraw2 = account.withdraw(moneyTransfer2);
        //
        assertThat(withdraw1.succeeded()).isTrue();
        assertThat(withdraw2.succeeded()).isFalse();
        assertThat(withdraw2.error().error()).isEqualTo("transaction-already-applied");
    }

    @Test
    public void should_credit_amount_and_generate_debit_transaction() {
        MoneyTransferService moneyTransferService = mock(MoneyTransferService.class);
        MoneyTransfer moneyTransfer = new MoneyTransfer(TRANSACTION_ID, ACCOUNT_ID1, ACCOUNT_ID2, M_100);

        Status<Failure, TransactionId> withdraw = account.withdraw(moneyTransfer);
        assertThat(withdraw.succeeded()).isTrue();

        account.applyTransactions(moneyTransferService);

        assertThat(account.balance()).isEqualTo(M_350.subtract(M_100));
        assertThat(account.transactions()).hasSize(1);
        Transaction tx = account.transactions().findFirst().get();
        assertThat(tx.transactionId()).isEqualTo(TRANSACTION_ID);
        assertThat(tx.status()).isEqualTo(TransactionStatus.Debited);
        verify(moneyTransferService).credit(Mockito.eq(moneyTransfer));
    }

}