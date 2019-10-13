package banktransfer.core.account;

import banktransfer.core.Failure;
import banktransfer.core.Status;

import java.util.Optional;
import java.util.function.Consumer;

public interface Accounts {
    Optional<Account> findById(AccountId accountId);

    Status<Failure, AccountId> add(NewAccount newAccount);

    void forEach(Consumer<Account> consumer);
}
