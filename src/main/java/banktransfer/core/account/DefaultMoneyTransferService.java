package banktransfer.core.account;

import banktransfer.core.Failure;
import banktransfer.core.Status;

import java.util.Optional;

public class DefaultMoneyTransferService implements MoneyTransferService, MoneyTransferSteps {
    private final Accounts accounts;

    public DefaultMoneyTransferService(Accounts accounts) {
        this.accounts = accounts;
    }

    @Override
    public Status<Failure, TransactionId> transfer(MoneyTransfer moneyTransfer) {
        AccountId srcId = moneyTransfer.source();
        AccountId dstId = moneyTransfer.destination();
        if (srcId.equals(dstId))
            return Status.failure("identical-src-dst");

        Optional<Account> srcOpt = accounts.findById(srcId);
        Optional<Account> dstOpt = accounts.findById(dstId);

        if (!srcOpt.isPresent())
            return Status.failure("unknown-src-account");
        if (!dstOpt.isPresent())
            return Status.failure("unknown-dst-account");

        Account account = srcOpt.get();
        return account.withdraws(moneyTransfer);
    }

    @Override
    public void credit(MoneyTransfer moneyTransfer) {
        Optional<Account> destinationOpt = accounts.findById(moneyTransfer.destination());

        Account account = destinationOpt.get();
        account.credits(moneyTransfer);
    }

    @Override
    public void acknowledge(MoneyTransfer moneyTransfer) {
        Optional<Account> sourceOpt = accounts.findById(moneyTransfer.source());

        Account account = sourceOpt.get();
        account.acknowledges(moneyTransfer);
    }

    /**
     * This method is single threaded, and is the only entry point
     * to perform credit/debit operation through transaction.
     */
    public synchronized void propagateTransactions() {
        accounts.forEach(this::propagateTransactions);
    }

    private void propagateTransactions(Account account) {
        account.applyTransactions(this);
    }
}
