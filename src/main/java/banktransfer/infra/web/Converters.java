package banktransfer.infra.web;

import banktransfer.core.Email;
import banktransfer.core.Failure;
import banktransfer.core.Status;
import banktransfer.core.account.Account;
import banktransfer.core.account.AccountId;
import banktransfer.core.account.MoneyTransfer;
import banktransfer.core.account.NewAccount;
import banktransfer.core.account.Transaction;
import banktransfer.core.account.TransactionId;
import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

class Converters {

    Status<Failure, NewAccount> toNewAccount(Supplier<JsonObject> rc) {
        JsonObject content;
        try {
            content = rc.get();
        } catch (Exception e) {
            return Status.failure("invalid-json-format");
        }

        Status<Failure, Email> emailOr;
        try {
            emailOr = Email.email(content.getString("email"));
        } catch (Exception e) {
            return Status.failure("invalid-email-format");
        }

        String amountStr;
        try {
            amountStr = content.getString("initial-balance");
        } catch (Exception e) {
            return Status.failure("invalid-amount-format");
        }

        if (amountStr == null)
            return NewAccount.newAccount(emailOr);
        return NewAccount.newAccount(emailOr, parseAmount(amountStr));
    }

    Status<Failure, MoneyTransfer> toMoneyTransfer(Supplier<JsonObject> rc) {
        JsonObject content;
        try {
            content = rc.get();
        } catch (Exception e) {
            return Status.failure("invalid-json-format");
        }

        Status<Failure, TransactionId> transactionIdOr;
        try {
            transactionIdOr = TransactionId.transactionId(content.getString("transaction-id"));
        } catch (Exception e) {
            return Status.failure("invalid-transaction-id-format");
        }

        Status<Failure, AccountId> accountSrcId;
        try {
            accountSrcId = AccountId.accountId(content.getString("source-id"));
        } catch (Exception e) {
            return Status.failure("invalid-source-id-format");
        }

        Status<Failure, AccountId> accountDstId;
        try {
            accountDstId = AccountId.accountId(content.getString("destination-id"));
        } catch (Exception e) {
            return Status.failure("invalid-destination-id-format");
        }


        Status<Failure, BigDecimal> amountOr;
        try {
            amountOr = parseAmount(content.getString("amount"));
        } catch (Exception e) {
            return Status.failure("invalid-amount-format");
        }

        return MoneyTransfer.moneyTransfert(transactionIdOr, accountSrcId, accountDstId, amountOr);
    }

    private Status<Failure, BigDecimal> parseAmount(String amount) {
        if (amount == null || amount.trim().isEmpty())
            return Status.failure("no-amount-provided");

        try {
            return Status.ok(new BigDecimal(amount));
        } catch (Exception e) {
            return Status.failure("invalid-amount-format");
        }
    }

    JsonObject toDto(Transaction tx) {
        return new JsonObject()
                .put("transaction-id", tx.transactionId().asString())
                .put("status", tx.status().name())
                .put("source-id", tx.moneyTransfer().source().asString())
                .put("destination-id", tx.moneyTransfer().destination().asString())
                .put("amount", tx.moneyTransfer().amount().toPlainString());
    }

    JsonObject toDto(Account account) {
        return new JsonObject()
                .put("account-id", account.accountId().asString())
                .put("balance", account.balance().toPlainString())
                .put("transactions", account.transactions().map(this::toDto).collect(toList()));
    }
}
