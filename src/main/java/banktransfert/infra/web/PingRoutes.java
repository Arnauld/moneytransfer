package banktransfert.infra.web;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import static banktransfert.infra.web.VertxTools.writeJson;
import static java.net.HttpURLConnection.HTTP_OK;

public class PingRoutes {
    public PingRoutes(Vertx vertx) {
    }

    public void init(Router router) {
        router.get("/ping").handler(rc -> {
            writeJson(rc, HTTP_OK, new JsonObject().put("status", "ok"));
        });
    }
}
