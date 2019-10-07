package banktransfert.core.account;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class AccountId {

    public static AccountId accountId(String asString) {
        if (asString == null)
            throw new IllegalArgumentException("AccountId cannot be null");
        return new AccountId(asString);
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
}
