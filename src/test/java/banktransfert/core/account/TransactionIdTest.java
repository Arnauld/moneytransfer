package banktransfert.core.account;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TransactionIdTest {

    @Test
    public void should_reject_null_or_empty_argument() {
        assertThat(TransactionId.transactionId(null).succeeded()).isFalse();
        assertThat(TransactionId.transactionId(null).error().error()).isEqualTo("no-transaction-id-provided");
        assertThat(TransactionId.transactionId("").succeeded()).isFalse();
        assertThat(TransactionId.transactionId("").error().error()).isEqualTo("no-transaction-id-provided");
        assertThat(TransactionId.transactionId("  ").succeeded()).isFalse();
        assertThat(TransactionId.transactionId("  ").error().error()).isEqualTo("no-transaction-id-provided");
    }

    @Test
    public void should_accept_valid_input() {
        assertThat(TransactionId.transactionId("w0001").succeeded()).isTrue();
        assertThat(TransactionId.transactionId("t0001").value().asString()).isEqualTo("t0001");
    }

    @Test
    public void should_ensure_consistency_with_equals_and_hashcode() {
        assertThat(TransactionId.transactionId("w0001").value()).isEqualTo(TransactionId.transactionId("w0001").value());
        assertThat(TransactionId.transactionId("w0001").value()).isNotEqualTo(TransactionId.transactionId("w0002").value());
        assertThat(TransactionId.transactionId("w0002").value()).isEqualTo(TransactionId.transactionId("w0002").value());
        assertThat(TransactionId.transactionId("w0001").value().hashCode()).isEqualTo(TransactionId.transactionId("w0001").value().hashCode());


    }
}