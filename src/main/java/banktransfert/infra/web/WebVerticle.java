package banktransfert.infra.web;

import banktransfert.core.account.AccountService;
import banktransfert.core.account.DefaultAccountService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class WebVerticle extends AbstractVerticle {
    public static final String HTTP_PORT = "http.port";
    //
    private final AccountService accountService;

    // default ctor is used by vertx
    public WebVerticle() {
        this(new DefaultAccountService());
    }

    public WebVerticle(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void start(Promise<Void> fut) {
        JsonObject config = context.config();

        Integer port = config.getInteger("http.port", 8080);

        Router router = initRouter();
        new PingRoutes(vertx).init(router);
        new AccountRoutes(vertx, accountService).init(router);
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(port, result -> {
                    if (result.succeeded()) {
                        fut.complete();
                    } else {
                        fut.fail(result.cause());
                    }
                });
    }

    protected Router initRouter() {
        Router router = Router.router(vertx);
        router.route().consumes("application/json");
        router.route().produces("application/json");
        router.route().handler(BodyHandler.create());
        return router;
    }

}
