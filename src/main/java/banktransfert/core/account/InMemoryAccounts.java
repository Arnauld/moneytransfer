package banktransfert.core.account;

import banktransfert.core.Email;
import banktransfert.core.Failure;
import banktransfert.core.Status;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class InMemoryAccounts implements Accounts {

    private final ConcurrentMap<Email, Account> accountByEmail = new ConcurrentHashMap<>();
    private final AccountIdGenerator idGenerator;

    public InMemoryAccounts(AccountIdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public Optional<Account> findById(AccountId accountId) {
        return Optional.empty();
    }

    @Override
    public Status<Failure, AccountId> create(NewAccount newAccount) {
        Email email = newAccount.email();
        AccountId accountId = idGenerator.newAccountId();
        Account account = new Account(accountId, email, newAccount.fullName());
        Account accountConcurrent = accountByEmail.putIfAbsent(email, account);
        if (accountConcurrent != null) {
            return Status.failure("email-already-inuse");
        }
        return Status.ok(accountId);
    }
}
