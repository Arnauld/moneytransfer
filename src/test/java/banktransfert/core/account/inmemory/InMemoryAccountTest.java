package banktransfert.core.account.inmemory;

import banktransfert.WAT;
import banktransfert.core.Failure;
import banktransfert.core.Status;
import banktransfert.core.account.AccountId;
import banktransfert.core.account.MoneyTransfer;
import banktransfert.core.account.MoneyTransferSteps;
import banktransfert.core.account.Transaction;
import banktransfert.core.account.TransactionId;
import banktransfert.core.account.TransactionStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class InMemoryAccountTest {
    private static final TransactionId TRANSACTION_ID = TransactionId.transactionId("t0001").value();
    private static final AccountId ACCOUNT_ID1 = AccountId.accountId("a0001").value();
    private static final AccountId ACCOUNT_ID2 = AccountId.accountId("a0002").value();
    private static final BigDecimal M_100 = BigDecimal.valueOf(100);
    private static final BigDecimal M_150 = BigDecimal.valueOf(150);
    private static final BigDecimal M_350 = BigDecimal.valueOf(350);

    private InMemoryAccount account1;
    private InMemoryAccount account2;

    @Before
    public void setUp() {
        account1 = new InMemoryAccount(ACCOUNT_ID1, M_350, emptyList());
        account2 = new InMemoryAccount(ACCOUNT_ID2, M_350, emptyList());
    }

    @Test
    public void should_provide_state_access() {
        assertThat(account1.accountId()).isEqualTo(ACCOUNT_ID1);
        assertThat(account1.balance()).isEqualTo(M_350);
        assertThat(account1.transactions()).hasSize(0);
    }

    @Test
    public void should_generate_transaction_on_withdraw_but_keep_balance_unchanged() {
        //
        account1.withdraws(new MoneyTransfer(TRANSACTION_ID, ACCOUNT_ID1, ACCOUNT_ID2, M_100));
        //
        assertThat(account1.balance()).isEqualTo(M_350);
        Transaction tx = findTransactionById(account1, TRANSACTION_ID);
        assertThat(tx.status()).isEqualTo(TransactionStatus.Pending);
    }

    @Test
    public void should_generate_transaction_on_credit_but_keep_balance_unchanged() {
        //
        account1.credits(new MoneyTransfer(TRANSACTION_ID, ACCOUNT_ID1, ACCOUNT_ID2, M_100));
        //
        assertThat(account1.balance()).isEqualTo(M_350);
        Transaction tx = findTransactionById(account1, TRANSACTION_ID);
        assertThat(tx.status()).isEqualTo(TransactionStatus.Pending);
    }

    @Test
    public void should_withdraw_only_when_source_account() {
        //
        MoneyTransfer moneyTransfer = new MoneyTransfer(TRANSACTION_ID, ACCOUNT_ID2, ACCOUNT_ID1, M_100);
        //
        Status<Failure, TransactionId> withdraw = account1.withdraws(moneyTransfer);
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
        Status<Failure, TransactionId> withdraw1 = account1.withdraws(moneyTransfer1);
        Status<Failure, TransactionId> withdraw2 = account1.withdraws(moneyTransfer2);
        //
        assertThat(withdraw1.succeeded()).isTrue();
        assertThat(withdraw2.succeeded()).isFalse();
        assertThat(withdraw2.error().error()).isEqualTo("transaction-already-applied");
    }

    @Test
    public void should_debit_amount_and_generate_credit_transaction() {
        MoneyTransferSteps moneyTransferSteps = mock(MoneyTransferSteps.class);
        MoneyTransfer moneyTransfer = new MoneyTransfer(TRANSACTION_ID, ACCOUNT_ID1, ACCOUNT_ID2, M_100);

        Status<Failure, TransactionId> withdraw = account1.withdraws(moneyTransfer);
        assertThat(withdraw.succeeded()).isTrue();

        account1.applyTransactions(moneyTransferSteps);

        assertThat(account1.balance()).isEqualTo(M_350.subtract(M_100));
        Transaction tx = findTransactionById(account1, TRANSACTION_ID);
        assertThat(tx.status()).isEqualTo(TransactionStatus.Debited);

        verify(moneyTransferSteps).credit(Mockito.eq(moneyTransfer));
    }

    @Test
    public void should_credit_amount_and_generate_acknowledge_transaction() {
        MoneyTransferSteps moneyTransferSteps = mock(MoneyTransferSteps.class);
        MoneyTransfer moneyTransfer = new MoneyTransfer(TRANSACTION_ID, ACCOUNT_ID1, ACCOUNT_ID2, M_100);

        Status<Failure, TransactionId> withdraw = account2.credits(moneyTransfer);
        assertThat(withdraw.succeeded()).isTrue();

        account2.applyTransactions(moneyTransferSteps);

        assertThat(account2.balance()).isEqualTo(M_350.add(M_100));
        Transaction tx = findTransactionById(account2, TRANSACTION_ID);
        assertThat(tx.status()).isEqualTo(TransactionStatus.Credited);

        verify(moneyTransferSteps).acknowledge(Mockito.eq(moneyTransfer));
    }

    @Test
    public void should_acknowledge_amount_and_nothing_else() {
        MoneyTransferSteps moneyTransferSteps = mock(MoneyTransferSteps.class);
        MoneyTransfer moneyTransfer = new MoneyTransfer(TRANSACTION_ID, ACCOUNT_ID1, ACCOUNT_ID2, M_100);

        // First make the transaction Credited
        account1.withdraws(moneyTransfer);
        account1.applyTransactions(moneyTransferSteps);
        assertThat(account1.balance()).isEqualTo(M_350.subtract(M_100));

        Status<Failure, TransactionId> acknowledges = account1.acknowledges(moneyTransfer);
        assertThat(acknowledges.succeeded()).isTrue();
        account1.applyTransactions(moneyTransferSteps);

        assertThat(account1.balance()).describedAs("Should remain unchanged").isEqualTo(M_350.subtract(M_100));
        Transaction tx = findTransactionById(account1, TRANSACTION_ID);
        assertThat(tx.status()).isEqualTo(TransactionStatus.Acknowledged);

        verify(moneyTransferSteps).credit(any());
        verifyNoMoreInteractions(moneyTransferSteps);
    }

    private Transaction findTransactionById(InMemoryAccount account, TransactionId transactionId) {
        Transaction tx = account.transactions().findFirst().orElseThrow(WAT::new);
        assertThat(tx.transactionId()).isEqualTo(transactionId);
        return tx;
    }

}