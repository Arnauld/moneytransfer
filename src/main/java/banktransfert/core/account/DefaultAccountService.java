package banktransfert.core.account;

import java.util.Optional;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class DefaultAccountService implements AccountService {
    @Override
    public Optional<Account> findById(AccountId accountId) {
        return Optional.empty();
    }
}
