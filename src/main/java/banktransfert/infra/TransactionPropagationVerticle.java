package banktransfert.infra;

import banktransfert.core.account.MoneyTransferService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static banktransfert.infra.Shared.sharedMoneyTransferService;

public class TransactionPropagationVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionPropagationVerticle.class);

    public static final String PROPAGATE_PERIOD_MS = "propagate-transactions.period-ms";

    private final MoneyTransferService moneyTransferService;


    // default ctor is used by vertx
    public TransactionPropagationVerticle() {
        this(sharedMoneyTransferService());
    }

    public TransactionPropagationVerticle(MoneyTransferService moneyTransferService) {
        this.moneyTransferService = moneyTransferService;
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        JsonObject config = context.config();
        vertx.setPeriodic(config.getInteger(PROPAGATE_PERIOD_MS, 1000), (time) -> {
            LOGGER.info("Propagating transactions...");
            moneyTransferService.propagateTransactions();
        });
        startPromise.complete();
    }
}
