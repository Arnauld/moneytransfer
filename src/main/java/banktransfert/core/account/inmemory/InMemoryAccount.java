package banktransfert.core.account.inmemory;

import banktransfert.core.account.AccountId;
import banktransfert.core.Failure;
import banktransfert.core.Status;
import banktransfert.core.account.Account;
import banktransfert.core.account.MoneyTransfer;
import banktransfert.core.account.MoneyTransferService;
import banktransfert.core.account.Transaction;
import banktransfert.core.account.TransactionId;
import banktransfert.core.account.TransactionStatus;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class InMemoryAccount implements Account {
    private final AtomicLong transactionSequence;
    private final AccountId accountId;
    private final AtomicReference<BigDecimal> balance;
    private ConcurrentMap<TransactionId, InMemoryTransaction> transactions = new ConcurrentHashMap<>();

    public InMemoryAccount(AccountId accountId, BigDecimal balance, List<InMemoryTransaction> transactions) {
        this.accountId = accountId;
        this.balance = new AtomicReference<>(balance);
        transactions.forEach(t -> this.transactions.put(t.transactionId(), t));
        this.transactionSequence = new AtomicLong(transactions.stream()
                .mapToLong(Transaction::sequence)
                .max().orElse(1));
    }

    private Stream<InMemoryTransaction> transactionStream() {
        return transactions.values()
                .stream()
                .sorted(Comparator.comparingLong(Transaction::sequence));
    }

    @Override
    public Stream<Transaction> transactions() {
        return transactionStream().map(Transaction.class::cast);
    }

    public AccountId accountId() {
        return accountId;
    }

    public BigDecimal balance() {
        return balance.get();
    }

    @Override
    public Status<Failure, TransactionId> withdraw(MoneyTransfer moneyTransfer) {
        if(!accountId.equals(moneyTransfer.source())) {
            return Status.failure("not-source-account");
        }

        TransactionId transactionId = moneyTransfer.transactionId();
        InMemoryTransaction transaction = new InMemoryTransaction(
                transactionSequence.incrementAndGet(),
                moneyTransfer,
                TransactionStatus.Pending);
        InMemoryTransaction transactionConcurrent = transactions.putIfAbsent(transactionId, transaction);
        if (transactionConcurrent != null) {
            return Status.failure("transaction-already-applied");
        }
        return Status.ok(transactionId);
    }

    @Override
    public void credit(MoneyTransfer moneyTransfer) {
        TransactionId transactionId = moneyTransfer.transactionId();
        InMemoryTransaction transaction = new InMemoryTransaction(
                transactionSequence.incrementAndGet(),
                moneyTransfer,
                TransactionStatus.Pending);
        transactions.putIfAbsent(transactionId, transaction);
    }

    @Override
    public synchronized void applyTransactions(MoneyTransferService moneyTransferService) {
        transactionStream()
                .filter(t -> t.status() == TransactionStatus.Pending)
                .forEach(t -> applyTransaction(t, moneyTransferService));
    }

    private void applyTransaction(InMemoryTransaction transaction, MoneyTransferService moneyTransferService) {
        BigDecimal balance = balance();
        BigDecimal amount = transaction.amountFor(accountId());
        BigDecimal newBalance = balance.add(amount);
        if (transaction.isSource(accountId)) {
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                transaction.cancel(Transaction.CancelReason.InsufficientFund);
                return;
            }

            this.balance.set(newBalance);
            transaction.debited();
            moneyTransferService.credit(transaction.moneyTransfer());
        } else {
            this.balance.set(newBalance);
            transaction.credited();
        }
    }
}
