package banktransfer.infra.web;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class VertxTools {
    public static void writeJson(RoutingContext rc, int httpStatus, JsonObject data) {
        rc.response()
                .setStatusCode(httpStatus)
                .putHeader("content-type", "application/json")
                .end(data.toString());
    }
}
