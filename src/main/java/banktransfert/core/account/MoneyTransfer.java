package banktransfert.core.account;

import banktransfert.core.Failure;
import banktransfert.core.Status;

import java.math.BigDecimal;

public class MoneyTransfer {

    public static Status<Failure, MoneyTransfer> moneyTransfert(Status<Failure, TransactionId> transactionIdOr,
                                                                Status<Failure, AccountId> accountSrcIdOr,
                                                                Status<Failure, AccountId> accountDstIdOr,
                                                                Status<Failure, BigDecimal> amountOr) {
        if (!transactionIdOr.succeeded())
            return Status.error(transactionIdOr.error());
        if (!accountSrcIdOr.succeeded())
            return Status.error(accountSrcIdOr.error());
        if (!accountDstIdOr.succeeded())
            return Status.error(accountDstIdOr.error());
        if (!amountOr.succeeded())
            return Status.error(amountOr.error());

        return Status.ok(new MoneyTransfer(transactionIdOr.value(),
                accountSrcIdOr.value(),
                accountDstIdOr.value(),
                amountOr.value()
        ));
    }


    private final TransactionId transactionId;
    private final AccountId source;
    private final AccountId destination;
    private final BigDecimal amount;

    public MoneyTransfer(TransactionId transactionId, AccountId source, AccountId destination, BigDecimal amount) {
        this.transactionId = transactionId;
        this.source = source;
        this.destination = destination;
        this.amount = amount;
    }

    public TransactionId transactionId() {
        return transactionId;
    }

    public AccountId source() {
        return source;
    }

    public AccountId destination() {
        return destination;
    }

    public BigDecimal amount() {
        return amount;
    }

    @Override
    public String toString() {
        return "MoneyTransfer{" + transactionId +
                "[" + source +
                ", " + destination +
                "] " + amount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MoneyTransfer that = (MoneyTransfer) o;

        if (!transactionId.equals(that.transactionId)) return false;
        if (!source.equals(that.source)) return false;
        if (!destination.equals(that.destination)) return false;
        return amount.equals(that.amount);
    }

    @Override
    public int hashCode() {
        int result = transactionId.hashCode();
        result = 31 * result + source.hashCode();
        result = 31 * result + destination.hashCode();
        result = 31 * result + amount.hashCode();
        return result;
    }
}
