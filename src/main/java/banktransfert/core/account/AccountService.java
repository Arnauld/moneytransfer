package banktransfert.core.account;

import java.util.Optional;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public interface AccountService {
    Optional<Account> findById(AccountId accountId);
}
