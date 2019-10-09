package banktransfert.core.account;

import banktransfert.core.Email;
import banktransfert.core.Failure;
import banktransfert.core.Status;

public class NewAccount {

    public static Status<Failure, NewAccount> newAccount(Status<Failure, Email> email, String fullName) {
        if (email.succeeded())
            return Status.ok(new NewAccount(email.value(), fullName));
        return Status.error(email.error());
    }

    private final Email email;
    private final String fullName;

    private NewAccount(Email email, String fullName) {
        this.email = email;
        this.fullName = fullName;
    }

    public Email email() {
        return email;
    }
}
