package banktransfert.core.account.inmemory;

import banktransfert.core.account.AccountId;
import banktransfert.core.account.MoneyTransfer;
import banktransfert.core.account.TransactionStatus;
import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Arrays;

import static banktransfert.core.account.Transaction.CancelReason.InsufficientFund;
import static banktransfert.core.account.TransactionStatus.Acknowledged;
import static banktransfert.core.account.TransactionStatus.Cancelled;
import static banktransfert.core.account.TransactionStatus.Credited;
import static banktransfert.core.account.TransactionStatus.Debited;
import static banktransfert.core.account.TransactionStatus.Pending;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class InMemoryTransactionTest {

    private static final BigDecimal M_250 = BigDecimal.valueOf(250);
    private MoneyTransfer moneyTransfer = Mockito.mock(MoneyTransfer.class);

    @Test
    public void should_flag_cancelled_only_pending_transaction() {
        for (TransactionStatus txStatus : Arrays.asList(Credited, Debited, Acknowledged, Cancelled)) {
            InMemoryTransaction tx = new InMemoryTransaction(1, moneyTransfer, txStatus);
            tx.cancel(InsufficientFund);
            assertThat(tx.status()).isEqualTo(txStatus);
        }

        InMemoryTransaction tx = new InMemoryTransaction(1, moneyTransfer, Pending);
        tx.cancel(InsufficientFund);
        assertThat(tx.status()).isEqualTo(Cancelled);
    }

    @Test
    public void should_flag_debited_only_pending_transaction() {
        for (TransactionStatus txStatus : Arrays.asList(Credited, Debited, Acknowledged, Cancelled)) {
            InMemoryTransaction tx = new InMemoryTransaction(1, moneyTransfer, txStatus);
            tx.debited();
            assertThat(tx.status()).isEqualTo(txStatus);
        }

        InMemoryTransaction tx = new InMemoryTransaction(1, moneyTransfer, Pending);
        tx.debited();
        assertThat(tx.status()).isEqualTo(Debited);
    }

    @Test
    public void should_flag_credited_only_pending_transaction() {
        for (TransactionStatus txStatus : Arrays.asList(Credited, Debited, Acknowledged, Cancelled)) {
            InMemoryTransaction tx = new InMemoryTransaction(1, moneyTransfer, txStatus);
            tx.credited();
            assertThat(tx.status()).isEqualTo(txStatus);
        }

        InMemoryTransaction tx = new InMemoryTransaction(1, moneyTransfer, Pending);
        tx.credited();
        assertThat(tx.status()).isEqualTo(Credited);
    }

    @Test
    public void should_return_money_transfer_amount_for_source() {
        AccountId aid1 = AccountId.accountId("w0001").value();
        AccountId aid2 = AccountId.accountId("w0002").value();
        when(moneyTransfer.amount()).thenReturn(M_250);
        when(moneyTransfer.source()).thenReturn(aid1);
        when(moneyTransfer.destination()).thenReturn(aid2);
        InMemoryTransaction tx = new InMemoryTransaction(1, moneyTransfer, Pending);

        assertThat(tx.isSource(aid1)).isTrue();
        assertThat(tx.isSource(aid1)).isTrue();
        assertThat(tx.amountFor(aid1)).isEqualTo(M_250.negate());
        assertThat(tx.amountFor(aid2)).isEqualTo(M_250);
    }

}