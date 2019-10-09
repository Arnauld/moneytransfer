package banktransfert.core.account;

import banktransfert.core.Email;
import banktransfert.core.Failure;
import banktransfert.core.Status;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static banktransfert.core.account.AccountId.accountId;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class InMemoryAccounts implements Accounts {

    private ConcurrentMap<Email, Account> accountByEmail = new ConcurrentHashMap<>();

    @Override
    public Optional<Account> findById(AccountId accountId) {
        return Optional.empty();
    }

    @Override
    public Status<Failure, AccountId> create(NewAccount newAccount) {
        Email email = newAccount.email();
        AccountId accountId = newAccountId();
        Account account = new Account(accountId, email, newAccount.fullName());
        Account accountConcurrent = accountByEmail.putIfAbsent(email, account);
        if (accountConcurrent != null) {
            return Status.failure("mid-air-collision");
        }
        return Status.ok(accountId);
    }

    private AccountId newAccountId() {
        return accountId(UUID.randomUUID().toString()).value();
    }
}
