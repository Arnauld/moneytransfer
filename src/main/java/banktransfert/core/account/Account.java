package banktransfert.core.account;

import banktransfert.core.Failure;
import banktransfert.core.Status;

import java.math.BigDecimal;
import java.util.stream.Stream;

public interface Account {
    AccountId accountId();

    BigDecimal balance();


}
