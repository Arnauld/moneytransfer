package banktransfert.infra.web;

import banktransfert.core.Email;
import banktransfert.core.Failure;
import banktransfert.core.Status;
import banktransfert.core.account.Account;
import banktransfert.core.account.AccountId;
import banktransfert.core.account.Accounts;
import banktransfert.core.account.NewAccount;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.Optional;

import static banktransfert.infra.web.VertxResponse.writeJson;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

public class AccountRoutes {
    private final Vertx vertx;
    private final Accounts accounts;

    public AccountRoutes(Vertx vertx, Accounts accounts) {
        this.vertx = vertx;
        this.accounts = accounts;
    }

    public void init(Router router) {
        router.get("/account/:accountId").handler(rc -> findAccountById(rc, rc.request().getParam("accountId")));
        router.post("/account").handler(this::createAccount);
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
        String fullName = content.getString("fullName");
        return NewAccount.newAccount(emailOr, fullName);
    }

    private JsonObject toDto(Account account) {
        return new JsonObject()
                .put("account-id", account.accountId().asString());
    }
}
