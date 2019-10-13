package banktransfer.infra;


import banktransfer.core.Email;
import banktransfer.core.Status;
import banktransfer.core.account.Account;
import banktransfer.core.account.AccountId;
import banktransfer.core.account.Accounts;
import banktransfer.core.account.MoneyTransferService;
import banktransfer.core.account.NewAccount;
import banktransfer.core.account.TransactionId;
import banktransfer.core.account.inmemory.InMemoryAccount;
import banktransfer.infra.web.WebVerticle;
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

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;

import static banktransfer.core.account.AccountId.accountId;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(VertxUnitRunner.class)
public class WebVerticleIntegrationTest {

    private static final Email EMAIL = Email.email("puck@tyrna.nog").value();
    private static final TransactionId TRANSACTION_ID = TransactionId.transactionId("t800").value();

    private Vertx vertx;
    private int port;
    private Accounts accounts;
    private MoneyTransferService moneyTransferService;

    @Before
    public void setUp(TestContext context) {
        accounts = Mockito.mock(Accounts.class);
        moneyTransferService = Mockito.mock(MoneyTransferService.class);
        //
        port = 50000 + new SecureRandom().nextInt(5000);
        vertx = Vertx.vertx();
        vertx.deployVerticle(
                new WebVerticle(accounts, moneyTransferService),
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

        Account account = new InMemoryAccount(accountId("w17").value(), BigDecimal.ZERO, emptyList());
        when(accounts.findById(Mockito.any())).thenReturn(Optional.of(account));

        WebClient client = WebClient.create(vertx);
        client.get(port, "localhost", "/account/w17")
                .putHeader("Content-Type", "application/json")
                .send(ar -> {
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
                .send(ar -> {
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
        client.get(port, "localhost", "/account/w17").send(ar -> {
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
    public void create_an_account_with_default_initial_amount(TestContext context) {
        final Async async = context.async();

        when(accounts.add(Mockito.any())).thenReturn(Status.ok(AccountId.accountId("x17").value()));

        WebClient client = WebClient.create(vertx);
        client.post(port, "localhost", "/account")
                .sendJsonObject(new JsonObject()
                        .put("email", "hog@tyrna.nog"), ar -> {
                    context.assertTrue(ar.succeeded());
                    HttpResponse<Buffer> response = ar.result();
                    context.assertEquals(201, response.statusCode());

                    JsonObject body = response.bodyAsJsonObject();
                    assertThat(body.getString("account-id")).isEqualTo("x17");

                    ArgumentCaptor<NewAccount> newAccountCaptor = ArgumentCaptor.forClass(NewAccount.class);
                    verify(accounts).add(newAccountCaptor.capture());
                    assertThat(newAccountCaptor.getValue().email()).isEqualTo(Email.email("hog@tyrna.nog").value());
                    assertThat(newAccountCaptor.getValue().initialAmount()).isEqualTo(BigDecimal.ZERO);
                    async.complete();
                });
    }

    @Test
    public void create_an_account_with_provided_initial_amount(TestContext context) {
        final Async async = context.async();

        when(accounts.add(Mockito.any())).thenReturn(Status.ok(AccountId.accountId("x17").value()));

        WebClient client = WebClient.create(vertx);
        client.post(port, "localhost", "/account")
                .sendJsonObject(new JsonObject()
                        .put("email", "hog@tyrna.nog")
                        .put("initial-amount", "500.5"), ar -> {
                    context.assertTrue(ar.succeeded());
                    HttpResponse<Buffer> response = ar.result();
                    context.assertEquals(201, response.statusCode());

                    JsonObject body = response.bodyAsJsonObject();
                    assertThat(body.getString("account-id")).isEqualTo("x17");

                    ArgumentCaptor<NewAccount> newAccountCaptor = ArgumentCaptor.forClass(NewAccount.class);
                    verify(accounts).add(newAccountCaptor.capture());
                    assertThat(newAccountCaptor.getValue().email()).isEqualTo(Email.email("hog@tyrna.nog").value());
                    assertThat(newAccountCaptor.getValue().initialAmount()).isEqualTo(new BigDecimal("500.5"));
                    async.complete();
                });
    }

    @Test
    public void create_an_account_with_an_invalid_json(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);
        client.post(port, "localhost", "/account")
                .sendBuffer(Buffer.buffer("{\"email\":}"), ar -> {
                    context.assertTrue(ar.succeeded());
                    HttpResponse<Buffer> response = ar.result();
                    context.assertEquals(400, response.statusCode());

                    JsonObject body = response.bodyAsJsonObject();
                    assertThat(body.getString("error")).isEqualTo("invalid-json-format");
                    verifyZeroInteractions(accounts);
                    async.complete();
                });
    }

    @Test
    public void create_an_account_with_an_invalid_initial_amount_format(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);
        client.post(port, "localhost", "/account")
                .sendJsonObject(new JsonObject()
                        .put("email", "hog@tyrna.nog")
                        .put("initial-amount", "500.5€"), ar -> {
                    context.assertTrue(ar.succeeded());
                    HttpResponse<Buffer> response = ar.result();
                    context.assertEquals(400, response.statusCode());

                    JsonObject body = response.bodyAsJsonObject();
                    assertThat(body.getString("error")).isEqualTo("invalid-amount-format");
                    verifyZeroInteractions(accounts);
                    async.complete();
                });
    }

    @Test
    public void create_an_account_with_an_missing_mandatory_data(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);
        client.post(port, "localhost", "/account")
                .sendBuffer(Buffer.buffer("{\"fullname\":\"Titania\"}"), ar -> {
                    context.assertTrue(ar.succeeded());
                    HttpResponse<Buffer> response = ar.result();
                    context.assertEquals(400, response.statusCode());

                    JsonObject body = response.bodyAsJsonObject();
                    assertThat(body.getString("error")).isEqualTo("no-email-provided");
                    verifyZeroInteractions(accounts);
                    async.complete();
                });
    }

    @Test
    public void transfert_money_between_existings_account(TestContext context) {
        final Async async = context.async();

        when(moneyTransferService.transfer(any())).thenReturn(Status.ok(TRANSACTION_ID));

        WebClient client = WebClient.create(vertx);
        client.post(port, "localhost", "/transfer")
                .sendJsonObject(new JsonObject()
                        .put("transaction-id", UUID.randomUUID().toString())
                        .put("source-id", "a001")
                        .put("destination-id", "a002")
                        .put("amount", "200"), ar -> {

                    context.assertTrue(ar.succeeded());
                    HttpResponse<Buffer> response = ar.result();
                    context.assertEquals(201, response.statusCode());

                    JsonObject body = response.bodyAsJsonObject();
                    assertThat(body.getString("transaction-id")).isNotBlank().isEqualTo("t800");
                    async.complete();
                });
    }

}