package podcast.model.exceptions;

public class AlreadyCreated extends RuntimeException {
    public AlreadyCreated(String message) {
        super(message);
    }
}
