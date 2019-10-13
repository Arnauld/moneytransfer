package banktransfert.core.account;

import banktransfert.core.Failure;
import banktransfert.core.Status;

public interface MoneyTransferService {

    Status<Failure, TransactionId> transfer(MoneyTransfer moneyTransfer);

    void propagateTransactions();

}
