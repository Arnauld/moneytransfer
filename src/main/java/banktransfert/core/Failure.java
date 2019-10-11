package banktransfert.core;

public class Failure {
    private final String error;

    public Failure(String error) {
        this.error = error;
    }

    public String error() {
        return error;
    }
}
