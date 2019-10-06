package banktransfert.infra.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;

public class WebVerticle extends AbstractVerticle {
    public static final String HTTP_PORT = "http.port";

    @Override
    public void start(Promise<Void> fut) {
        JsonObject config = context.config();

        Integer port = config.getInteger("http.port", 8080);

        vertx.createHttpServer()
                .requestHandler(r -> {
                    r.response().end("<h1>Hello from my first " +
                            "Vert.x 3 application</h1>");
                })
                .listen(port, result -> {
                    if (result.succeeded()) {
                        fut.complete();
                    } else {
                        fut.fail(result.cause());
                    }
                });
    }

}
