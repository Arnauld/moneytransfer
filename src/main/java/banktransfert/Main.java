package banktransfert;

import banktransfert.infra.web.WebVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

public class Main {

    /**
     * Environment variable: http.port
     */
    public static final String HTTP_PORT = WebVerticle.HTTP_PORT;

    /**
     * Environment variable: http.instances
     */
    public static final String HTTP_INSTANCES = "http.instances";

    public static void main(String[] args) {
        System.setProperty("org.vertx.logger-delegate-factory-class-name",
                "org.vertx.java.core.logging.impl.SLF4JLogDelegateFactory");
        VertxOptions vertxOptions = new VertxOptions();
        Vertx vertx = Vertx.vertx(vertxOptions);
        vertx.deployVerticle(WebVerticle.class, webDeploymentOptions());
    }

    private static DeploymentOptions webDeploymentOptions() {
        int httpPort = parseInt(System.getenv(HTTP_PORT), 8083);
        int httpInstances = parseInt(System.getenv(HTTP_INSTANCES), 2);
        return new DeploymentOptions()
                .setInstances(httpInstances)
                .setConfig(new JsonObject().put(HTTP_PORT, httpPort));
    }

    private static int parseInt(String value, int defaultValue) {
        if (value == null || value.isEmpty())
            return defaultValue;
        return Integer.parseInt(value);
    }
}
