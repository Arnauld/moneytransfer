package banktransfert.core.account;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class Account {
    private final AccountId accountId;

    public Account(AccountId accountId) {
        this.accountId = accountId;
    }

    public AccountId accountId() {
        return accountId;
    }
}
