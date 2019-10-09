package banktransfert.infra;


import banktransfert.core.Email;
import banktransfert.core.Status;
import banktransfert.core.account.Account;
import banktransfert.core.account.AccountId;
import banktransfert.core.account.Accounts;
import banktransfert.core.account.NewAccount;
import banktransfert.infra.web.WebVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.security.SecureRandom;
import java.util.Optional;

import static banktransfert.core.account.AccountId.accountId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(VertxUnitRunner.class)
public class WebVerticleTest {

    private Vertx vertx;
    private int port;
    private Accounts accounts;

    @Before
    public void setUp(TestContext context) {
        accounts = Mockito.mock(Accounts.class);
        //
        port = 50000 + new SecureRandom().nextInt(5000);
        vertx = Vertx.vertx();
        vertx.deployVerticle(
                new WebVerticle(accounts),
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
    public void ping(TestContext context) {
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

        when(accounts.findById(Mockito.any())).thenReturn(Optional.of(new Account(accountId("w17").value())));

        WebClient client = WebClient.create(vertx);
        client.get(port, "localhost", "/account/w17")
                .putHeader("Content-Type", "application/json")
                .send(
                        ar -> {
                            assertThat(ar.succeeded()).isTrue();
                            HttpResponse<Buffer> response = ar.result();
                            assertThat(response.statusCode()).isEqualTo(200);
                            assertThat(response.getHeader("Content-Type")).isEqualTo("application/json");
                            JsonObject body = response.bodyAsJsonObject();
                            assertThat(body.getString("account-id")).isEqualTo("w17");
                            async.complete();
                        });
    }

    @Test
    public void consult_with_an_invalid_account_id(TestContext context) {
        final Async async = context.async();

        assertThat(accountId("w123456789w123456789w123456789w123456789").succeeded()).isFalse();

        when(accounts.findById(Mockito.any())).thenReturn(Optional.empty());

        WebClient client = WebClient.create(vertx);
        client.get(port, "localhost", "/account/w123456789w123456789w123456789w123456789")
                .putHeader("Content-Type", "application/json")
                .send(
                        ar -> {
                            context.assertTrue(ar.succeeded());
                            HttpResponse<Buffer> response = ar.result();
                            context.assertEquals(400, response.statusCode());
                            context.assertEquals("application/json", response.getHeader("Content-Type"));
                            JsonObject body = response.bodyAsJsonObject();
                            context.assertEquals("invalid-account-id", body.getString("error"));
                            context.assertEquals("w123456789w123456789w123456789w123456789", body.getString("account-id"));
                            async.complete();
                        });
    }

    @Test
    public void consult_an_unexisting_account(TestContext context) {
        final Async async = context.async();

        when(accounts.findById(Mockito.any())).thenReturn(Optional.empty());

        WebClient client = WebClient.create(vertx);
        client.get(port, "localhost", "/account/w17").send(
                ar -> {
                    context.assertTrue(ar.succeeded());
                    HttpResponse<Buffer> response = ar.result();
                    context.assertEquals(404, response.statusCode());
                    JsonObject body = response.bodyAsJsonObject();
                    context.assertTrue(body.getString("error").contains("account-not-found"));
                    context.assertTrue(body.getString("account-id").contains("w17"));
                    async.complete();
                });
    }

    @Test
    public void create_an_account(TestContext context) {
        final Async async = context.async();

        when(accounts.create(Mockito.any())).thenReturn(Status.ok(AccountId.accountId("x17").value()));

        WebClient client = WebClient.create(vertx);
        client.post(port, "localhost", "/account")
                .sendJsonObject(new JsonObject()
                                .put("email", "hog@tyrna.nog"),
                        ar -> {
                            context.assertTrue(ar.succeeded());
                            HttpResponse<Buffer> response = ar.result();
                            context.assertEquals(201, response.statusCode());
                            
                            JsonObject body = response.bodyAsJsonObject();
                            assertThat(body.getString("account-id")).isEqualTo("x17");

                            ArgumentCaptor<NewAccount> newAccountCaptor = ArgumentCaptor.forClass(NewAccount.class);
                            verify(accounts).create(newAccountCaptor.capture());
                            assertThat(newAccountCaptor.getValue().email()).isEqualTo(Email.email("hog@tyrna.nog").value());
                            async.complete();
                        });
    }

}