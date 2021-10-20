package pandorum.models;

import java.util.HashMap;
import java.util.Map;

import com.mongodb.reactivestreams.client.MongoCollection;

import org.bson.Document;

import pandorum.database.MongoSchema;
import pandorum.database.NonRequired;
import pandorum.database.Required;

public class PlayerInfo extends MongoSchema<String, Object> {
    public PlayerInfo(MongoCollection<Document> collection) {
        super(
            collection,
            new Required<>("uuid", String.class),
            new Required<>("hellomsg", Boolean.class),
            new Required<>("alerts", Boolean.class),
            new Required<>("playtime", Long.class),
            new Required<>("buildingsBuilt", Integer.class),
            new Required<>("buildingsDeconstructed", Integer.class),
            new Required<>("maxWave", Integer.class),
            new Required<>("gamesPlayed", Integer.class),
            new NonRequired<>("locale", String.class)
        );
    }

    public Document create(String uuid, Boolean hellomsg, Boolean alerts, String locale, long playtime, int built, int deconstructed, int wave, int games) {
        HashMap<String, Object> schema = new HashMap<>(Map.of(
                "uuid", uuid,
                "hellomsg", hellomsg,
                "alerts", alerts,
                "playtime", playtime,
                "buildingsBuilt", built,
                "buildingsDeconstructed", deconstructed,
                "maxWave", wave,
                "gamesPlayed", games
        ));

        schema.put("locale", locale);
        return this.create(schema);
    }
}
