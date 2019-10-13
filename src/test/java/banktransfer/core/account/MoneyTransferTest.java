package banktransfer.core.account;

import org.junit.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class MoneyTransferTest {

    private static final TransactionId TRANSACTION_ID1 = TransactionId.transactionId("t0001").value();
    private static final TransactionId TRANSACTION_ID2 = TransactionId.transactionId("t0002").value();
    private static final AccountId ACCOUNT_ID1 = AccountId.accountId("a0001").value();
    private static final AccountId ACCOUNT_ID2 = AccountId.accountId("a0002").value();
    private static final AccountId ACCOUNT_ID3 = AccountId.accountId("a0003").value();
    private static final BigDecimal M_100 = BigDecimal.valueOf(100);
    private static final BigDecimal M_150 = BigDecimal.valueOf(150);
    private static final BigDecimal M_350 = BigDecimal.valueOf(350);

    @Test
    public void should_ensure_consistency_with_equals_and_hashcode() {
        MoneyTransfer m1a = new MoneyTransfer(TRANSACTION_ID1, ACCOUNT_ID1, ACCOUNT_ID2, M_100);
        MoneyTransfer m1b = new MoneyTransfer(TRANSACTION_ID1, ACCOUNT_ID1, ACCOUNT_ID2, M_100);
        MoneyTransfer m2a = new MoneyTransfer(TRANSACTION_ID2, ACCOUNT_ID1, ACCOUNT_ID2, M_100);
        MoneyTransfer m2b = new MoneyTransfer(TRANSACTION_ID1, ACCOUNT_ID2, ACCOUNT_ID2, M_100);
        MoneyTransfer m2c = new MoneyTransfer(TRANSACTION_ID1, ACCOUNT_ID1, ACCOUNT_ID3, M_100);
        MoneyTransfer m2d = new MoneyTransfer(TRANSACTION_ID1, ACCOUNT_ID1, ACCOUNT_ID3, M_150);
        assertThat(m1a).isEqualTo(m1b);
        assertThat(m1a.hashCode()).isEqualTo(m1b.hashCode());
        assertThat(m1a).isNotEqualTo(m2a);
        assertThat(m1a).isNotEqualTo(m2b);
        assertThat(m1a).isNotEqualTo(m2c);
        assertThat(m1a).isNotEqualTo(m2d);
    }

}