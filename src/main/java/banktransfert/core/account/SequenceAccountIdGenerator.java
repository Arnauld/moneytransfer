package banktransfert.core.account;

import java.util.concurrent.atomic.AtomicLong;

public class SequenceAccountIdGenerator implements AccountIdGenerator {

    private final AtomicLong seqGen = new AtomicLong();

    @Override
    public AccountId newAccountId() {
        return AccountId.accountId("w" + seqGen.incrementAndGet()).value();
    }
}
