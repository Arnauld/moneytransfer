package banktransfer.core.account;

import banktransfer.core.Failure;
import banktransfer.core.Status;

public class TransactionId {
    public static Status<Failure, TransactionId> transactionId(String asString) {
        if (asString == null || asString.trim().isEmpty())
            return Status.failure("no-transaction-id-provided");
        if (asString.length() > 36)
            return Status.failure("invalid-transaction-id-length");
        return Status.ok(new TransactionId(asString));
    }

    private final String raw;

    private TransactionId(String raw) {
        this.raw = raw;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        TransactionId accountId = (TransactionId) o;
        return raw.equals(accountId.raw);
    }

    @Override
    public int hashCode() {
        return raw.hashCode();
    }

    public String asString() {
        return raw;
    }

    @Override
    public String toString() {
        return "TransactionId{" +
                "raw='" + raw + '\'' +
                '}';
    }
}
