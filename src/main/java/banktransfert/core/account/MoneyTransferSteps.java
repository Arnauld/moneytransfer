package banktransfert.core.account;

public interface MoneyTransferSteps {
    void credit(MoneyTransfer moneyTransfer);

    void acknowledge(MoneyTransfer moneyTransfer);
}
