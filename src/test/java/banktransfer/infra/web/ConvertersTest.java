package banktransfer.infra.web;

import banktransfer.core.Email;
import banktransfer.core.Failure;
import banktransfer.core.Status;
import banktransfer.core.account.MoneyTransfer;
import banktransfer.core.account.NewAccount;
import io.vertx.core.json.JsonObject;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.function.BiConsumer;

import static banktransfer.core.account.AccountId.accountId;
import static banktransfer.core.account.TransactionId.transactionId;
import static org.assertj.core.api.Assertions.assertThat;

public class ConvertersTest {

    private Converters converters;

    @Before
    public void setUp() {
        converters = new Converters();
    }

    @Test
    public void should_convert_valid_NewAccount__no_initial_amount_provided() {
        JsonObject json = new JsonObject().put("email", "titania@@tyrna.nog");

        Status<Failure, NewAccount> newAccountOr = converters.toNewAccount(() -> json);

        assertThat(newAccountOr.succeeded()).isTrue();
        assertThat(newAccountOr.value().initialAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(newAccountOr.value().email()).isEqualTo(Email.email("titania@@tyrna.nog").value());
    }


    @Test
    public void should_convert_valid_NewAccount__with_initial_amount_provided() {
        JsonObject json = new JsonObject().put("email", "titania@@tyrna.nog").put("initial-amount", "500.5");

        Status<Failure, NewAccount> newAccountOr = converters.toNewAccount(() -> json);

        assertThat(newAccountOr.succeeded()).isTrue();
        assertThat(newAccountOr.value().initialAmount()).isEqualTo(new BigDecimal("500.5"));
        assertThat(newAccountOr.value().email()).isEqualTo(Email.email("titania@@tyrna.nog").value());
    }

    @Test
    public void should_not_convert_invalid_NewAccount__invalid_json_provided() {
        Status<Failure, NewAccount> newAccountOr = converters.toNewAccount(() -> {
            throw new RuntimeException("BOOM");
        });

        assertThat(newAccountOr.succeeded()).isFalse();
        assertThat(newAccountOr.error().error()).isEqualTo("invalid-json-format");
    }

    @Test
    public void should_not_convert_invalid_NewAccount__invalid_email_provided() {
        JsonObject json = new JsonObject().put("email", "titania");

        Status<Failure, NewAccount> newAccountOr = converters.toNewAccount(() -> json);

        assertThat(newAccountOr.succeeded()).isFalse();
        assertThat(newAccountOr.error().error()).isEqualTo("invalid-email");
    }

    @Test
    public void should_not_convert_invalid_NewAccount__invalid_email_provided__wrong_type() {
        JsonObject json = new JsonObject().put("email", true);

        Status<Failure, NewAccount> newAccountOr = converters.toNewAccount(() -> json);

        assertThat(newAccountOr.succeeded()).isFalse();
        assertThat(newAccountOr.error().error()).isEqualTo("invalid-email-format");
    }

    @Test
    public void should_not_convert_invalid_NewAccount__invalid_amount_format_provided() {
        JsonObject json = new JsonObject().put("email", "titania@tyrna.nog").put("initial-amount", "500€");

        Status<Failure, NewAccount> newAccountOr = converters.toNewAccount(() -> json);

        assertThat(newAccountOr.succeeded()).isFalse();
        assertThat(newAccountOr.error().error()).isEqualTo("invalid-amount-format");
    }

    @Test
    public void should_not_convert_invalid_NewAccount__invalid_amount_format_provided__wrong_type() {
        JsonObject json = new JsonObject().put("email", "titania@tyrna.nog").put("initial-amount", true);

        Status<Failure, NewAccount> newAccountOr = converters.toNewAccount(() -> json);

        assertThat(newAccountOr.succeeded()).isFalse();
        assertThat(newAccountOr.error().error()).isEqualTo("invalid-amount-format");
    }

    @Test
    public void should_convert_valid_MoneyTransfer() {
        JsonObject json = new JsonObject()
                .put("transaction-id", "t0001")
                .put("source-id", "a001")
                .put("destination-id", "a002")
                .put("amount", "500.7");

        Status<Failure, MoneyTransfer> moneyTransferOr = converters.toMoneyTransfer(() -> json);
        assertThat(moneyTransferOr.succeeded()).isTrue();
        assertThat(moneyTransferOr.value().transactionId()).isEqualTo(transactionId("t0001").value());
        assertThat(moneyTransferOr.value().source()).isEqualTo(accountId("a001").value());
        assertThat(moneyTransferOr.value().destination()).isEqualTo(accountId("a002").value());
        assertThat(moneyTransferOr.value().amount()).isEqualTo(new BigDecimal("500.7"));
    }

    @Test
    public void should_not_convert_invalid_MoneyTransfer__invalid_json_provided() {
        Status<Failure, MoneyTransfer> moneyTransferOr = converters.toMoneyTransfer(() -> {
            throw new RuntimeException("BOOM");
        });

        assertThat(moneyTransferOr.succeeded()).isFalse();
        assertThat(moneyTransferOr.error().error()).isEqualTo("invalid-json-format");
    }


    @Test
    public void should_not_convert_invalid_MoneyTransfer__invalid_type_provided() {
        JsonObject base = new JsonObject()
                .put("transaction-id", "t0001")
                .put("source-id", "a001")
                .put("destination-id", "a002")
                .put("amount", "500.7");

        BiConsumer<JsonObject, String> assertInvalidJson = (json, error) -> {
            Status<Failure, MoneyTransfer> status = converters.toMoneyTransfer(() -> json);
            assertThat(status.succeeded()).isFalse();
            assertThat(status.error().error()).isEqualTo(error);

        };

        assertInvalidJson.accept(base.copy().put("transaction-id", true), "invalid-transaction-id-format");
        assertInvalidJson.accept(base.copy().put("source-id", true), "invalid-source-id-format");
        assertInvalidJson.accept(base.copy().put("destination-id", true), "invalid-destination-id-format");
        assertInvalidJson.accept(base.copy().put("amount", true), "invalid-amount-format");
    }


    @Test
    public void should_not_convert_invalid_MoneyTransfer__invalid_values_provided() {
        JsonObject base = new JsonObject()
                .put("transaction-id", "t0001")
                .put("source-id", "a001")
                .put("destination-id", "a002")
                .put("amount", "500.7");

        BiConsumer<JsonObject, String> assertInvalidJson = (json, error) -> {
            Status<Failure, MoneyTransfer> status = converters.toMoneyTransfer(() -> json);
            assertThat(status.succeeded()).isFalse();
            assertThat(status.error().error()).isEqualTo(error);

        };

        assertInvalidJson.accept(base.copy().put("transaction-id", ""), "no-transaction-id-provided");
        assertInvalidJson.accept(base.copy().put("source-id", ""), "no-account-id-provided");
        assertInvalidJson.accept(base.copy().put("destination-id", ""), "no-account-id-provided");
        assertInvalidJson.accept(base.copy().put("amount", ""), "no-amount-provided");
    }


}