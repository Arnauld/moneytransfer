package banktransfert.infra.web;

import banktransfert.core.account.Accounts;
import banktransfert.core.account.DefaultAccounts;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class WebVerticle extends AbstractVerticle {
    public static final String HTTP_PORT = "http.port";
    private static final long MAX_BODY_SIZE_IN_BYTES = 4048;
    private static final String APPLICATION_JSON = "application/json";
    //
    private final Accounts accounts;

    // default ctor is used by vertx
    public WebVerticle() {
        this(new DefaultAccounts());
    }

    public WebVerticle(Accounts accounts) {
        this.accounts = accounts;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        JsonObject config = context.config();

        Integer port = config.getInteger("http.port", 8080);

        Router router = initRouter();
        new PingRoutes(vertx).init(router);
        new AccountRoutes(vertx, accounts).init(router);
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(port, result -> {
                    if (result.succeeded()) {
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
        return router;
    }

}