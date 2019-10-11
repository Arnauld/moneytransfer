package banktransfert.core.account;

import banktransfert.core.Email;
import banktransfert.core.Failure;
import banktransfert.core.Status;

import java.math.BigDecimal;

public class NewAccount {

    public static Status<Failure, NewAccount> newAccount(Status<Failure, Email> email) {
        return newAccount(email, BigDecimal.ZERO);
    }

    public static Status<Failure, NewAccount> newAccount(Status<Failure, Email> email,
                                                         BigDecimal initialAmount) {
        if (email.succeeded())
            return Status.ok(new NewAccount(email.value(), initialAmount));
        return Status.error(email.error());
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
