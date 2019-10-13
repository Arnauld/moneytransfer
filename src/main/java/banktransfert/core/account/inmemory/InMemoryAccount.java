package banktransfert.core.account.inmemory;

import banktransfert.core.Failure;
import banktransfert.core.Status;
import banktransfert.core.account.*;

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
    public Status<Failure, TransactionId> withdraws(MoneyTransfer moneyTransfer) {
        if (!accountId.equals(moneyTransfer.source())) {
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
    public Status<Failure, TransactionId> credits(MoneyTransfer moneyTransfer) {
        TransactionId transactionId = moneyTransfer.transactionId();
        InMemoryTransaction transaction = new InMemoryTransaction(
                transactionSequence.incrementAndGet(),
                moneyTransfer,
                TransactionStatus.Pending);
        InMemoryTransaction previous = transactions.putIfAbsent(transactionId, transaction);
        if (previous != null)
            return Status.failure("transaction-already-applied");
        return Status.ok(transactionId);
    }

    @Override
    public Status<Failure, TransactionId> acknowledges(MoneyTransfer moneyTransfer) {
        TransactionId transactionId = moneyTransfer.transactionId();
        InMemoryTransaction transaction = transactions.get(transactionId);
        if (transaction == null)
            return Status.failure("transaction-unknown");
        return transaction.acknowledged() ? Status.ok(transactionId) : Status.failure("ack-failed");
    }

    @Override
    public synchronized void applyTransactions(MoneyTransferSteps moneyTransferService) {
        transactionStream()
                .filter(t -> t.status() == TransactionStatus.Pending)
                .forEach(t -> applyTransaction(t, moneyTransferService));
    }

    private void applyTransaction(InMemoryTransaction transaction, MoneyTransferSteps moneyTransferService) {
        BigDecimal balance = balance();
        BigDecimal amount = transaction.amountFor(accountId());
        BigDecimal newBalance = balance.add(amount);
        if (transaction.isSource(accountId)) {
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                transaction.cancel(Transaction.CancelReason.InsufficientFund);
                return;
            }

            if (transaction.debited()) {
                this.balance.set(newBalance);
                moneyTransferService.credit(transaction.moneyTransfer());
            }
        } else {
            if (transaction.credited()) {
                this.balance.set(newBalance);
                moneyTransferService.acknowledge(transaction.moneyTransfer());
            }
        }
    }
}
