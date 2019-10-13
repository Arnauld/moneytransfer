package banktransfer.core.account.inmemory;

import banktransfer.core.account.*;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static banktransfer.core.Email.email;
import static banktransfer.core.Status.ok;
import static org.assertj.core.api.Assertions.assertThat;

public class ConcurrencyUsecaseTest {

    private InMemoryAccounts accounts;
    private DefaultMoneyTransferService moneyTransferService;

    @Before
    public void setUp() {
        accounts = new InMemoryAccounts(new SequenceAccountIdGenerator());
        moneyTransferService = new DefaultMoneyTransferService(accounts);
    }

    @Test
    public void concurrency_cases() throws InterruptedException {
        SecureRandom random = new SecureRandom();
        int nbAccount = 50;
        List<AccountId> accountIds = fillWithRandomAccounts(random, nbAccount);

        BigDecimal totalBefore = totalBalance(accounts);

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

        ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(moneyTransferService::propagateTransactions, 10, 10, TimeUnit.MILLISECONDS);

        int nbTransfers = 5000;
        for (int i = 0; i < nbTransfers; i++) {
            final int j = i;
            executorService.execute(() -> {
                AccountId id1 = accountIds.get(random.nextInt(nbAccount));
                AccountId id2 = accountIds.get(random.nextInt(nbAccount));
                TransactionId tid = TransactionId.transactionId("t" + j).value();
                MoneyTransfer moneyTransfer = new MoneyTransfer(tid, id1, id2, BigDecimal.valueOf(random.nextInt(50)));
                moneyTransferService.transfer(moneyTransfer);
            });
        }

        // wait for all transfers
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
        scheduledFuture.cancel(true);

        // make sure all transaction have been processed
        moneyTransferService.propagateTransactions(); // may generate credit
        moneyTransferService.propagateTransactions(); // may generate ack
        // make sure all transaction have been acknowledged
        moneyTransferService.propagateTransactions();

        //
//        accounts.forEach(c -> {
//            System.out.println(c.accountId() + "::" + c.balance());
//            c.transactions().forEach(t -> System.out.println("    " + t));
//        });

        //
        BigDecimal totalAfter = totalBalance(accounts);
        assertThat(totalAfter).describedAs("No money should have been created...").isEqualTo(totalBefore);

        accounts.forEach(a -> assertTransactions(a));
    }

    private void assertTransactions(Account a) {
        EnumSet<TransactionStatus> ACCEPTED_STATUSES =
                EnumSet.of(TransactionStatus.Cancelled,
                        TransactionStatus.Credited,
                        TransactionStatus.Acknowledged);
        assertThat(a.transactions()
                .filter(t -> !ACCEPTED_STATUSES.contains(t.status()))).hasSize(0);
    }

    private AccountId newAccount(String email, BigDecimal initialAmount) {
        return accounts.add(NewAccount.newAccount(email(email), ok(initialAmount)).value()).value();
    }

    private List<AccountId> fillWithRandomAccounts(SecureRandom random, int nbAccount) {
        return IntStream.range(0, nbAccount)
                .mapToObj(i -> {
                    String email = "w" + i + "@w.com";
                    return newAccount(email, BigDecimal.valueOf(random.nextInt(500)));
                })
                .collect(Collectors.toList());
    }

    private static BigDecimal totalBalance(Accounts accounts) {
        AtomicReference<BigDecimal> total = new AtomicReference<>(BigDecimal.ZERO);
        accounts.forEach(a -> {
            BigDecimal current = total.get();
            total.set(current.add(a.balance()));
        });
        return total.get();
    }
}
