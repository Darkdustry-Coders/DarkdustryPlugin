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
            new Required<>("permission", Integer.class),
            new Required<>("hellomsg", Boolean.class),
            new Required<>("alerts", Boolean.class),
            new Required<>("playtime", Long.class),
            new Required<>("buildingsBuilt", Long.class),
            new Required<>("buildingsDeconstructed", Long.class),
            new Required<>("wavesSurvived", Integer.class),
            new Required<>("gamesWon", Integer.class),
            new NonRequired<>("locale", String.class)
        );
    }

    public Document create(String uuid, int permission, Boolean hellomsg, Boolean alerts, String locale, long playtime, long built, long deconstructed, int waves, int games) {
        HashMap<String, Object> schema = new HashMap<>(Map.of(
                "uuid", uuid,
                "permission", permission,
                "hellomsg", hellomsg,
                "alerts", alerts,
                "playtime", playtime,
                "buildingsBuilt", built,
                "buildingsDeconstructed", deconstructed,
                "wavesSurvived", waves,
                "gamesWon", games
        ));

        schema.put("locale", locale);
        return this.create(schema);
    }
}
