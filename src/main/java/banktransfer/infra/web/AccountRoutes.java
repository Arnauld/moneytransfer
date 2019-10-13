package banktransfer.infra.web;

import banktransfer.core.Failure;
import banktransfer.core.Status;
import banktransfer.core.account.*;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static banktransfer.infra.web.VertxTools.writeJson;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

public class AccountRoutes {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountRoutes.class);

    private final Vertx vertx;
    private final Converters converters;
    private final Accounts accounts;
    private final MoneyTransferService moneyTransferService;

    public AccountRoutes(Vertx vertx,
                         Converters converters,
                         Accounts accounts,
                         MoneyTransferService moneyTransferService) {
        this.vertx = vertx;
        this.converters = converters;
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
        Status<Failure, MoneyTransfer> moneyTransferOr = converters.toMoneyTransfer(rc::getBodyAsJson);
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
        LOGGER.info("About to create account...");
        Status<Failure, NewAccount> newAccountOr = converters.toNewAccount(rc::getBodyAsJson);
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
        LOGGER.info("About to lookup account...");
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
        writeJson(rc, HTTP_OK, converters.toDto(account));
    }

    private void replyInvalidAccountId(RoutingContext rc, String rawAccountId) {
        writeJson(rc, HTTP_BAD_REQUEST,
                new JsonObject()
                        .put("error", "invalid-account-id")
                        .put("account-id", rawAccountId));
    }

}
