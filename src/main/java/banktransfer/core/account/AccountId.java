package banktransfer.core.account;

import banktransfer.core.Failure;
import banktransfer.core.Status;

public class AccountId {

    public static Status<Failure, AccountId> accountId(String asString) {
        if (asString == null || asString.trim().isEmpty())
            return Status.failure("no-account-id-provided");
        if (asString.length() > 36)
            return Status.failure("invalid-account-id-length");
        return Status.ok(new AccountId(asString));
    }

    private final String raw;

    private AccountId(String raw) {
        this.raw = raw;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        AccountId accountId = (AccountId) o;
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
        return "AccountId{" +
                "raw='" + raw + '\'' +
                '}';
    }
}
