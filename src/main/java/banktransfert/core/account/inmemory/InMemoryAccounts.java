package banktransfert.core.account.inmemory;

import banktransfert.core.Email;
import banktransfert.core.Failure;
import banktransfert.core.Status;
import banktransfert.core.account.Account;
import banktransfert.core.account.AccountId;
import banktransfert.core.account.AccountIdGenerator;
import banktransfert.core.account.Accounts;
import banktransfert.core.account.NewAccount;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public class InMemoryAccounts implements Accounts {

    private final ConcurrentMap<Email, InMemoryAccount> accountByEmail = new ConcurrentHashMap<>();
    private final AccountIdGenerator idGenerator;

    public InMemoryAccounts(AccountIdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public Optional<Account> findById(AccountId accountId) {
        return accountByEmail.values()
                .stream()
                .filter(a -> a.accountId().equals(accountId))
                .map(Account.class::cast)
                .findFirst();
    }

    @Override
    public Status<Failure, AccountId> add(NewAccount newAccount) {
        Email email = newAccount.email();
        AccountId accountId = idGenerator.newAccountId();
        InMemoryAccount account = new InMemoryAccount(accountId, newAccount.initialAmount(), Collections.emptyList());
        InMemoryAccount accountConcurrent = accountByEmail.putIfAbsent(email, account);
        if (accountConcurrent != null) {
            return Status.failure("email-already-inuse");
        }
        return Status.ok(accountId);
    }

    @Override
    public void forEach(Consumer<Account> consumer) {
        accountByEmail.values().forEach(consumer);
    }
}
