package banktransfert.infra.web;

import banktransfert.core.account.Account;
import banktransfert.core.account.AccountId;
import banktransfert.core.account.AccountService;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.Optional;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class AccountRoutes {
    private final Vertx vertx;
    private final AccountService accountService;

    public AccountRoutes(Vertx vertx, AccountService accountService) {
        this.vertx = vertx;
        this.accountService = accountService;
    }

    public void init(Router router) {
        router.get("/account/:accountId").handler(rc -> findAccountById(rc, rc.request().getParam("accountId")));
    }

    private void findAccountById(RoutingContext rc, String rawAccountId) {
        AccountId accountId = AccountId.accountId(rawAccountId);
        Optional<Account> accountOpt = accountService.findById(accountId);

        if (accountOpt.isPresent()) {
            rc.response()
                    .setStatusCode(HTTP_OK)
                    .end(toDto(accountOpt.get()).toString());
        } else {
            rc.response()
                    .setStatusCode(HTTP_NOT_FOUND)
                    .end(new JsonObject()
                            .put("error", "account-not-found")
                            .put("account-id", rawAccountId).toString());

        }
    }

    private JsonObject toDto(Account account) {
        return new JsonObject()
                .put("account-id", account.accountId().asString());
    }
}
