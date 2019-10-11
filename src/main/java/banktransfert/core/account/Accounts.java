package banktransfert.core.account;

import banktransfert.core.Failure;
import banktransfert.core.Status;

import java.util.Optional;
import java.util.function.Consumer;

public interface Accounts {
    Optional<Account> findById(AccountId accountId);

    Status<Failure, AccountId> add(NewAccount newAccount);

    void forEach(Consumer<Account> consumer);
}
