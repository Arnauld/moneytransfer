package banktransfer.core.account;

import banktransfer.core.Failure;
import banktransfer.core.Status;

import java.math.BigDecimal;
import java.util.stream.Stream;

public interface Account {
    AccountId accountId();

    BigDecimal balance();

    void applyTransactions(MoneyTransferSteps moneyTransferService);

    Stream<Transaction> transactions();

    Status<Failure, TransactionId> withdraws(MoneyTransfer moneyTransfer);

    Status<Failure, TransactionId> acknowledges(MoneyTransfer moneyTransfer);

    Status<Failure, TransactionId> credits(MoneyTransfer moneyTransfer);
}
