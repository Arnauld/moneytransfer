package banktransfer.core.account;

import banktransfer.core.Failure;
import banktransfer.core.Status;

public interface MoneyTransferService {

    Status<Failure, TransactionId> transfer(MoneyTransfer moneyTransfer);

    void propagateTransactions();

}
