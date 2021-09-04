package pandorum.database.events;

import org.reactivestreams.Subscription;

public class OnSubscribe {
    public Subscription subscription;
    
    public OnSubscribe(Subscription subscription) {
        this.subscription = subscription;
    }
}