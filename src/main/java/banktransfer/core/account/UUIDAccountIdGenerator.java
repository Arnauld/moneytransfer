package banktransfer.core.account;

import java.util.UUID;

import static banktransfer.core.account.AccountId.accountId;

public class UUIDAccountIdGenerator implements AccountIdGenerator {
    @Override
    public AccountId newAccountId() {
        return accountId(UUID.randomUUID().toString()).value();
    }
}
