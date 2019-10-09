package banktransfert.core;

public class Email {

    public static Status<Failure, Email> email(String raw) {
        if (raw == null || raw.trim().isEmpty())
            return Status.failure("no-email-provided");
        if (!raw.matches(".+@.+\\.[a-zA-Z0-9]+"))
            return Status.failure("invalid-email");
        return Status.ok(new Email(raw.toLowerCase()));
    }

    private final String raw;

    public Email(String raw) {
        this.raw = raw;
    }

    public String asString() {
        return raw;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Email email = (Email) o;

        return raw.equals(email.raw);
    }

    @Override
    public int hashCode() {
        return raw.hashCode();
    }

    @Override
    public String toString() {
        return "Email{" +
                "raw='" + raw + '\'' +
                '}';
    }
}
