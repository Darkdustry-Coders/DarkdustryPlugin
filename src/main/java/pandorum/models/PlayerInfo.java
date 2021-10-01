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
            new Required<>("banned", Boolean.class),
            new Required<>("playtime", Long.class),
            new NonRequired<>("locale", String.class)
        );
    }

    public Document create(String uuid, Boolean hellomsg, Boolean banned, String locale, long playtime) {
        HashMap<String, Object> schema = new HashMap<>(Map.of(
                "uuid", uuid,
                "hellomsg", hellomsg,
                "banned", banned,
                "playtime", playtime
        ));

        schema.put("locale", locale);
        return this.create(schema);
    }
}
