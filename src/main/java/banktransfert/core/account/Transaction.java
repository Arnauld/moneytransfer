package banktransfert.core.account;

import java.math.BigDecimal;

public interface Transaction {

    BigDecimal amountFor(AccountId accountId);

    enum CancelReason {
        InsufficientFund,
        None
    }

    long sequence();

    TransactionStatus status();

    TransactionId transactionId();

    MoneyTransfer moneyTransfer();

}
