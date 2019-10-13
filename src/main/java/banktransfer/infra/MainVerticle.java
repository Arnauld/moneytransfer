package banktransfer.infra;

import banktransfer.infra.web.WebVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;

import java.util.Arrays;

import static banktransfer.infra.web.WebVerticle.HTTP_PORT;

public class MainVerticle extends AbstractVerticle {

    public static final String HTTP_INSTANCES = "http.instances";

    @Override
    public void start(Promise<Void> startPromise) {
        JsonObject config = context.config();

        Promise<String> webPromise = Promise.promise();
        Promise<String> txPromise = Promise.promise();


        vertx.deployVerticle(WebVerticle.class, webDeploymentOptions(config), webPromise);
        vertx.deployVerticle(TransactionPropagationVerticle.class, transactionPropagationDeploymentOptions(config), txPromise);

        CompositeFuture.all(Arrays.asList(webPromise.future(), txPromise.future())).setHandler(ar -> {
            if (ar.succeeded())
                startPromise.complete();
            else
                startPromise.fail(ar.cause());
        });
    }

    private static DeploymentOptions transactionPropagationDeploymentOptions(JsonObject config) {
        int propagationPeriodMs = config.getInteger(TransactionPropagationVerticle.PROPAGATE_PERIOD_MS);
        return new DeploymentOptions()
                .setInstances(1)
                .setConfig(new JsonObject().put(TransactionPropagationVerticle.PROPAGATE_PERIOD_MS, propagationPeriodMs));
    }

    private static DeploymentOptions webDeploymentOptions(JsonObject config) {
        int httpPort = config.getInteger(HTTP_PORT);
        int httpInstances = config.getInteger(HTTP_INSTANCES);
        return new DeploymentOptions()
                .setInstances(httpInstances)
                .setConfig(new JsonObject().put(HTTP_PORT, httpPort));
    }
}
