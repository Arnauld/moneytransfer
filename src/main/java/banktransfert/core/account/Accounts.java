package banktransfert.core.account;

import banktransfert.core.Failure;
import banktransfert.core.Status;

import java.util.Optional;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public interface Accounts {
    Optional<Account> findById(AccountId accountId);

    Status<Failure, AccountId> add(NewAccount newAccount);
}
