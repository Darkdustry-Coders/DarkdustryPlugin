package pandorum.database.events;

public class OnError {
    public Throwable error;

    public OnError(Throwable error) {
        this.error = error;
    }
}