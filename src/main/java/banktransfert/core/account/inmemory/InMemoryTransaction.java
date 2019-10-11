package banktransfert.core.account.inmemory;

import banktransfert.core.account.AccountId;
import banktransfert.core.account.MoneyTransfer;
import banktransfert.core.account.Transaction;
import banktransfert.core.account.TransactionId;
import banktransfert.core.account.TransactionStatus;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

public class InMemoryTransaction implements Transaction {

    private final long sequence;
    private final MoneyTransfer moneyTransfer;
    private final AtomicReference<TransactionStatus> status;
    private CancelReason cancelReason = CancelReason.None;

    public InMemoryTransaction(long sequence,
                               MoneyTransfer moneyTransfer,
                               TransactionStatus transactionStatus) {
        this.sequence = sequence;
        this.moneyTransfer = moneyTransfer;
        this.status = new AtomicReference<>(transactionStatus);
    }

    @Override
    public BigDecimal amountFor(AccountId accountId) {
        return isSource(accountId) ? moneyTransfer.amount().negate() : moneyTransfer.amount();
    }

    @Override
    public MoneyTransfer moneyTransfer() {
        return moneyTransfer;
    }

    public boolean isSource(AccountId accountId) {
        return moneyTransfer.source().equals(accountId);
    }

    public long sequence() {
        return sequence;
    }

    public TransactionStatus status() {
        return status.get();
    }

    public TransactionId transactionId() {
        return moneyTransfer.transactionId();
    }

    void cancel(CancelReason cancelReason) {
        if (status.compareAndSet(TransactionStatus.Pending, TransactionStatus.Cancelled)) {
            this.cancelReason = cancelReason;
        }
    }

    void credited() {
        status.compareAndSet(TransactionStatus.Pending, TransactionStatus.Credited);
    }

    void debited() {
        status.compareAndSet(TransactionStatus.Pending, TransactionStatus.Debited);
    }

    @Override
    public String toString() {
        return "InMemoryTransaction{" +
                "sequence=" + sequence +
                ", status=" + status +
                ", cancelReason=" + cancelReason +
                ", moneyTransfer=" + moneyTransfer +
                '}';
    }
}
