package banktransfert.core.account;

import banktransfert.core.Email;

public class NewAccount {
    private final Email email;
    private final String fullName;

    public NewAccount(Email email, String fullName) {
        this.email = email;
        this.fullName = fullName;
    }

    public Email email() {
        return email;
    }
}
