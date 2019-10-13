package banktransfert.infra.web;

import banktransfert.core.account.Accounts;
import banktransfert.core.account.MoneyTransferService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static banktransfert.infra.Shared.sharedInMemoryAccounts;
import static banktransfert.infra.Shared.sharedMoneyTransferService;

public class WebVerticle extends AbstractVerticle {
    public static final String HTTP_PORT = "http.port";
    private static final long MAX_BODY_SIZE_IN_BYTES = 4048;
    private static final String APPLICATION_JSON = "application/json";
    //
    private static final Logger LOGGER = LoggerFactory.getLogger(WebVerticle.class);

    private final Accounts accounts;
    private final MoneyTransferService moneyTransferService;

    // default ctor is used by vertx
    public WebVerticle() {
        // whereas this is an ugly approach, this overcomes the usage of multiple WebVerticle
        // by ensuring all of them use the same underlying Repository, even concurrently...
        // next step would be to use a dedicated Verticle for the Repository
        this(sharedInMemoryAccounts(), sharedMoneyTransferService());
    }

    public WebVerticle(Accounts accounts, MoneyTransferService moneyTransferService) {
        this.accounts = accounts;
        this.moneyTransferService = moneyTransferService;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        JsonObject config = context.config();

        Integer port = config.getInteger("http.port", 8080);

        Router router = initRouter();
        new PingRoutes(vertx).init(router);
        new AccountRoutes(vertx, new Converters(), accounts, moneyTransferService).init(router);
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(port, result -> {
                    if (result.succeeded()) {
                        LOGGER.info("WebVerticle stated on localhost:{}", port);
                        startPromise.complete();
                    } else {
                        startPromise.fail(result.cause());
                    }
                });
    }

    protected Router initRouter() {
        Router router = Router.router(vertx);
        router.route().consumes(APPLICATION_JSON);
        router.route().produces(APPLICATION_JSON);
        router.route().handler(BodyHandler.create().setBodyLimit(MAX_BODY_SIZE_IN_BYTES));
        router.errorHandler(500, rc -> {
            LOGGER.error("Oops", rc.failure());
        });
        return router;
    }

}
