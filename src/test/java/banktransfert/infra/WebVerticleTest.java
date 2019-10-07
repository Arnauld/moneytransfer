package banktransfert.infra;


import banktransfert.core.account.Account;
import banktransfert.core.account.AccountService;
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
import org.mockito.Mockito;

import java.security.SecureRandom;
import java.util.Optional;

import static banktransfert.core.account.AccountId.accountId;
import static org.mockito.Mockito.when;

@RunWith(VertxUnitRunner.class)
public class WebVerticleTest {

    private Vertx vertx;
    private int port;
    private AccountService accountService;

    @Before
    public void setUp(TestContext context) {
        accountService = Mockito.mock(AccountService.class);
        //
        port = 50000 + new SecureRandom().nextInt(5000);
        vertx = Vertx.vertx();
        vertx.deployVerticle(
                new WebVerticle(accountService),
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
        client.get(port, "localhost", "/ping").send(
                ar -> {
                    context.assertTrue(ar.succeeded());
                    context.assertTrue(ar.result().bodyAsString().contains("status"));
                    context.assertTrue(ar.result().bodyAsString().contains("ok"));
                    async.complete();
                });
    }

    @Test
    public void consult_an_existing_account(TestContext context) {
        final Async async = context.async();

        when(accountService.findById(Mockito.any())).thenReturn(Optional.of(new Account(accountId("w17"))));

        WebClient client = WebClient.create(vertx);
        client.get(port, "localhost", "/account/w17").send(
                ar -> {
                    context.assertTrue(ar.succeeded());
                    System.out.println(ar.result().bodyAsString());
                    context.assertTrue(ar.result().bodyAsString().contains("\"w17\""));
                    async.complete();
                });
    }

}