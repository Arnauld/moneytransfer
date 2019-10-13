package banktransfer.core.account;

import banktransfer.core.Email;
import banktransfer.core.Failure;
import banktransfer.core.Status;

import java.math.BigDecimal;

public class NewAccount {

    public static Status<Failure, NewAccount> newAccount(Status<Failure, Email> email) {
        return newAccount(email, Status.ok(BigDecimal.ZERO));
    }

    public static Status<Failure, NewAccount> newAccount(Status<Failure, Email> email,
                                                         Status<Failure, BigDecimal> initialAmount) {
        if (!email.succeeded())
            return Status.error(email.error());
        if (!initialAmount.succeeded())
            return Status.error(initialAmount.error());
        return Status.ok(new NewAccount(email.value(), initialAmount.value()));
    }

    private final Email email;
    private final BigDecimal initialAmount;

    private NewAccount(Email email, BigDecimal initialAmount) {
        this.email = email;
        this.initialAmount = initialAmount;
    }

    public Email email() {
        return email;
    }

    public BigDecimal initialAmount() {
        return initialAmount;
    }
}
