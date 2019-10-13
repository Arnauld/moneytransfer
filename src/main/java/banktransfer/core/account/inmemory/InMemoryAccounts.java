package banktransfer.core.account.inmemory;

import banktransfer.core.Email;
import banktransfer.core.Failure;
import banktransfer.core.Status;
import banktransfer.core.account.Account;
import banktransfer.core.account.AccountId;
import banktransfer.core.account.AccountIdGenerator;
import banktransfer.core.account.Accounts;
import banktransfer.core.account.NewAccount;

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
