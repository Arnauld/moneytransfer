package banktransfer;

import banktransfer.infra.MainVerticle;
import banktransfer.infra.TransactionPropagationVerticle;
import banktransfer.infra.web.WebVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

public class Main {

    /**
     * Environment variable: http.port
     */
    public static final String HTTP_PORT = "http.port";

    /**
     * Environment variable: http.instances
     */
    public static final String HTTP_INSTANCES = "http.instances";

    /**
     * Environment variable: propagate-transactions.period-ms
     *
     * @param args
     */
    public static final String PROPAGATE_PERIOD_MS = "propagate-transactions.period-ms";

    public static void main(String[] args) {
        System.setProperty("org.vertx.logger-delegate-factory-class-name",
                "org.vertx.java.core.logging.impl.SLF4JLogDelegateFactory");
        VertxOptions vertxOptions = new VertxOptions();
        Vertx vertx = Vertx.vertx(vertxOptions);
        vertx.deployVerticle(MainVerticle.class, deploymentOptions());
    }


    private static DeploymentOptions deploymentOptions() {
        int httpPort = parseInt(System.getenv(HTTP_PORT), 8083);
        int httpInstances = parseInt(System.getenv(HTTP_INSTANCES), 2);
        int propagatePeriodMs = parseInt(System.getenv(PROPAGATE_PERIOD_MS), 1000);
        return new DeploymentOptions()
                .setInstances(1)
                .setConfig(new JsonObject()
                        .put(WebVerticle.HTTP_PORT, httpPort)
                        .put(MainVerticle.HTTP_INSTANCES, httpInstances)
                        .put(TransactionPropagationVerticle.PROPAGATE_PERIOD_MS, propagatePeriodMs)
                )
                ;
    }

    private static int parseInt(String value, int defaultValue) {
        if (value == null || value.isEmpty())
            return defaultValue;
        return Integer.parseInt(value);
    }
}
