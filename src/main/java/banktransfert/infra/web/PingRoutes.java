package banktransfert.infra.web;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class PingRoutes {
    public PingRoutes(Vertx vertx) {
    }

    public void init(Router router) {
        router.get("/ping").handler(rc -> {
            rc.response()
                    .setStatusCode(HTTP_OK)
                    .end(new JsonObject().put("status", "ok").toString());
        });
    }
}
