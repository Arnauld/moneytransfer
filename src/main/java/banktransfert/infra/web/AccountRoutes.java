package banktransfert.infra.web;

import banktransfert.core.Email;
import banktransfert.core.Failure;
import banktransfert.core.Status;
import banktransfert.core.account.*;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Optional;

import static banktransfert.infra.web.VertxResponse.writeJson;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

public class AccountRoutes {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountRoutes.class);

    private final Vertx vertx;
    private final Accounts accounts;
    private final MoneyTransferService moneyTransferService;

    public AccountRoutes(Vertx vertx, Accounts accounts, MoneyTransferService moneyTransferService) {
        this.vertx = vertx;
        this.accounts = accounts;
        this.moneyTransferService = moneyTransferService;
    }

    public void init(Router router) {
        router.get("/account/:accountId").handler(rc -> findAccountById(rc, rc.request().getParam("accountId")));
        router.post("/account").handler(this::createAccount);
        router.post("/transfer").handler(this::transfer);
    }

    private void transfer(RoutingContext rc) {
        LOGGER.info("About to transfer money...");
        Status<Failure, MoneyTransfer> moneyTransferOr = toMoneyTransfer(rc);
        if (!moneyTransferOr.succeeded()) {
            replyInvalidTransfer(rc, moneyTransferOr.error());
            return;
        }

        MoneyTransfer moneyTransfer = moneyTransferOr.value();
        Status<Failure, TransactionId> transferOr = moneyTransferService.transfer(moneyTransfer);

        if (!transferOr.succeeded()) {
            replyTransferFailed(rc, transferOr.error());
            return;
        }
        replyTransferCreated(rc, transferOr.value());
    }

    private void replyTransferCreated(RoutingContext rc, TransactionId transactionId) {
        writeJson(rc, HTTP_CREATED,
                new JsonObject().put("transaction-id", transactionId.asString()));
    }

    private void replyTransferFailed(RoutingContext rc, Failure error) {
        writeJson(rc, HTTP_BAD_REQUEST,
                new JsonObject()
                        .put("error", "transfer-failed")
                        .put("details", error.error()));
    }

    private void replyInvalidTransfer(RoutingContext rc, Failure error) {
        writeJson(rc, HTTP_BAD_REQUEST,
                new JsonObject()
                        .put("error", "invalid-transfer")
                        .put("details", error.error()));
    }

    private void createAccount(RoutingContext rc) {
        Status<Failure, NewAccount> newAccountOr = toNewAccount(rc);
        if (!newAccountOr.succeeded()) {
            replyInvalidNewAccount(rc, newAccountOr.error());
            return;
        }
        Status<Failure, AccountId> createdOr = accounts.add(newAccountOr.value());
        if (!createdOr.succeeded()) {
            replyNewAccountFailed(rc, createdOr.error());
            return;
        }
        replyAccountCreated(rc, createdOr.value());
    }

    private void replyAccountCreated(RoutingContext rc, AccountId accountId) {
        writeJson(rc, HTTP_CREATED,
                new JsonObject().put("account-id", accountId.asString()));
    }

    private void replyNewAccountFailed(RoutingContext rc, Failure error) {
        writeJson(rc, HTTP_BAD_REQUEST,
                new JsonObject().put("error", error.error()));
    }

    private void replyInvalidNewAccount(RoutingContext rc, Failure error) {
        writeJson(rc, HTTP_BAD_REQUEST,
                new JsonObject().put("error", error.error()));
    }

    private void findAccountById(RoutingContext rc, String rawAccountId) {
        Status<Failure, AccountId> accountIdOr = AccountId.accountId(rawAccountId);
        if (!accountIdOr.succeeded()) {
            replyInvalidAccountId(rc, rawAccountId);
            return;
        }

        Optional<Account> accountOpt = accounts.findById(accountIdOr.value());
        if (accountOpt.isPresent()) {
            replyAccountFound(rc, accountOpt.get());
        } else {
            replyAccountNotFound(rc, rawAccountId);
        }
    }

    private void replyAccountNotFound(RoutingContext rc, String rawAccountId) {
        writeJson(rc, HTTP_NOT_FOUND,
                new JsonObject()
                        .put("error", "account-not-found")
                        .put("account-id", rawAccountId));
    }

    private void replyAccountFound(RoutingContext rc, Account account) {
        writeJson(rc, HTTP_OK, toDto(account));
    }

    private void replyInvalidAccountId(RoutingContext rc, String rawAccountId) {
        writeJson(rc, HTTP_BAD_REQUEST,
                new JsonObject()
                        .put("error", "invalid-account-id")
                        .put("account-id", rawAccountId));
    }

    private Status<Failure, NewAccount> toNewAccount(RoutingContext rc) {
        JsonObject content;
        try {
            content = rc.getBodyAsJson();
        } catch (Exception e) {
            return Status.failure("invalid-json-format");
        }

        Status<Failure, Email> emailOr = Email.email(content.getString("email"));

        String amountStr = content.getString("initial-amount");
        if(amountStr==null)
            return NewAccount.newAccount(emailOr);

        return NewAccount.newAccount(emailOr, parseAmount(amountStr));
    }

    private Status<Failure, MoneyTransfer> toMoneyTransfer(RoutingContext rc) {
        JsonObject content;
        try {
            content = rc.getBodyAsJson();
        } catch (Exception e) {
            return Status.failure("invalid-json-format");
        }

        Status<Failure, TransactionId> transactionIdOr = TransactionId.transactionId(content.getString("transaction-id"));
        Status<Failure, AccountId> accountSrcId = AccountId.accountId(content.getString("source-id"));
        Status<Failure, AccountId> accountDstId = AccountId.accountId(content.getString("destination-id"));
        Status<Failure, BigDecimal> amountOr = parseAmount(content.getString("amount"));

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

    private JsonObject toDto(Account account) {
        return new JsonObject()
                .put("account-id", account.accountId().asString());
    }
}
