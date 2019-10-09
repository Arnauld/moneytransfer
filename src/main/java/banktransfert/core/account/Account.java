package banktransfert.core.account;

import banktransfert.core.Email;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class Account {
    private final AccountId accountId;

    public Account(AccountId accountId, Email email, String fullName) {
        this.accountId = accountId;
    }

    public AccountId accountId() {
        return accountId;
    }
}
