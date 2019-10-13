package banktransfert.infra;

import banktransfert.core.account.Accounts;
import banktransfert.core.account.DefaultMoneyTransferService;
import banktransfert.core.account.MoneyTransferService;
import banktransfert.core.account.UUIDAccountIdGenerator;
import banktransfert.core.account.inmemory.InMemoryAccounts;

public class Shared {
    //
    private static Accounts SINGLETON;

    public static synchronized Accounts sharedInMemoryAccounts() {
        if (SINGLETON == null)
            SINGLETON = new InMemoryAccounts(new UUIDAccountIdGenerator());
        return SINGLETON;
    }

    public static MoneyTransferService sharedMoneyTransferService() {
        return new DefaultMoneyTransferService(sharedInMemoryAccounts());
    }
}
