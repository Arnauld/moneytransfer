package banktransfert.infra;

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

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;

@RunWith(VertxUnitRunner.class)
public class UsecasesIntegrationTest {

    private static final BigDecimal M_500 = BigDecimal.valueOf(500);
    private static final BigDecimal M_450 = BigDecimal.valueOf(450);
    private static final BigDecimal M_150 = BigDecimal.valueOf(150);

    private int port;
    private Vertx vertx;
    //
    private WebClient client;
    //
    private AtomicReference<String> accountId1 = new AtomicReference<>();
    private AtomicReference<String> accountId2 = new AtomicReference<>();

    @Before
    public void setUp(TestContext context) {
        port = 50000 + new SecureRandom().nextInt(5000);
        vertx = Vertx.vertx();
        vertx.deployVerticle(
                MainVerticle.class,
                new DeploymentOptions()
                        .setInstances(1)
                        .setConfig(new JsonObject()
                                .put(WebVerticle.HTTP_PORT, port)
                                .put(MainVerticle.HTTP_INSTANCES, 2)
                                .put(TransactionPropagationVerticle.PROPAGATE_PERIOD_MS, 200)),
                context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void basic_scenario(TestContext context) {
        final Async async = context.async();
        client = WebClient.create(vertx);

        createAccount(context, "titania@tyrna.nog", M_500, accountId1);
        createAccount(context, "oberon@tyrna.nog", M_450, accountId2);

        String transactionId = UUID.randomUUID().toString();
        transferMoney(context, transactionId, accountId1, accountId2, M_150);

        await().atMost(2, TimeUnit.SECONDS).until(() -> balanceOf(context, accountId1).equals(M_500.subtract(M_150)));
        await().atMost(2, TimeUnit.SECONDS).until(() -> balanceOf(context, accountId2).equals(M_450.add(M_150)));

        async.complete();
    }

    private BigDecimal balanceOf(TestContext context, AtomicReference<String> accountId) {
        AtomicReference<BigDecimal> balance = new AtomicReference<>();
        CyclicBarrier internalBarrier = new CyclicBarrier(2);
        client.get(port, "localhost", "/account/" + accountId.get())
                .send(ar -> {
                    context.assertTrue(ar.succeeded());
                    HttpResponse<Buffer> response = ar.result();
                    context.assertEquals(200, response.statusCode());
                    JsonObject body = response.bodyAsJsonObject();
                    balance.set(new BigDecimal(body.getString("balance")));
                    barrierAwait(internalBarrier);

                });
        barrierAwait(internalBarrier);
        return balance.get();
    }

    private void transferMoney(TestContext context,
                               String transactionId,
                               AtomicReference<String> accountIdSrc,
                               AtomicReference<String> accountIdDst,
                               BigDecimal amount) {
        client.post(port, "localhost", "/transfer")
                .sendJsonObject(new JsonObject()
                        .put("transaction-id", transactionId)
                        .put("source-id", accountIdSrc.get())
                        .put("destination-id", accountIdDst.get())
                        .put("amount", amount.toPlainString()), ar -> {

                    context.assertTrue(ar.succeeded());
                    HttpResponse<Buffer> response = ar.result();
                    context.assertEquals(201, response.statusCode());
                });
    }

    private void createAccount(TestContext context,
                               String email,
                               BigDecimal initialAmount,
                               AtomicReference<String> accountIdRef) {
        CyclicBarrier internalBarrier = new CyclicBarrier(2);
        client.post(port, "localhost", "/account")
                .sendJsonObject(new JsonObject()
                        .put("email", email)
                        .put("initial-amount", initialAmount.toPlainString()), ar -> {

                    context.assertTrue(ar.succeeded());
                    HttpResponse<Buffer> response = ar.result();
                    context.assertEquals(201, response.statusCode());
                    JsonObject body = response.bodyAsJsonObject();
                    accountIdRef.set(body.getString("account-id"));
                    barrierAwait(internalBarrier);
                });
        barrierAwait(internalBarrier);
    }

    private static void barrierAwait(CyclicBarrier barrier) {
        try {
            barrier.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
