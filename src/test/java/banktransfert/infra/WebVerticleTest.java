package banktransfert.infra;


import banktransfert.infra.web.WebVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.SecureRandom;

@RunWith(VertxUnitRunner.class)
public class WebVerticleTest {

    private Vertx vertx;
    private int port;

    @Before
    public void setUp(TestContext context) {
        port = 50000 + new SecureRandom().nextInt(5000);
        vertx = Vertx.vertx();
        vertx.deployVerticle(
                WebVerticle.class.getName(),
                new DeploymentOptions()
                        .setInstances(1)
                        .setConfig(new JsonObject().put(WebVerticle.HTTP_PORT, port)),
                context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testMyApplication(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);
        client.get(port, "localhost", "/").send(
                ar -> {
                    context.assertTrue(ar.succeeded());
                    context.assertTrue(ar.result().bodyAsString().contains("Hello"));
                    async.complete();
                });
    }

}