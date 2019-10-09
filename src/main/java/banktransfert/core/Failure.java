package banktransfert.core;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class Failure {
    private final String error;

    public Failure(String error) {
        this.error = error;
    }

    public String error() {
        return error;
    }
}
