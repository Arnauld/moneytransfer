package banktransfer.infra;

import banktransfer.core.account.Accounts;
import banktransfer.core.account.DefaultMoneyTransferService;
import banktransfer.core.account.MoneyTransferService;
import banktransfer.core.account.UUIDAccountIdGenerator;
import banktransfer.core.account.inmemory.InMemoryAccounts;

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
