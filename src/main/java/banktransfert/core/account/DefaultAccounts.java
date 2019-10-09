package banktransfert.core.account;

import banktransfert.core.Failure;
import banktransfert.core.Status;

import java.util.Optional;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class DefaultAccounts implements Accounts {
    @Override
    public Optional<Account> findById(AccountId accountId) {
        return Optional.empty();
    }

    @Override
    public Status<Failure, AccountId> create(NewAccount newAccount) {
        return Status.failure("closed");
    }
}
