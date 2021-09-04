package pandorium.models;

import java.util.HashMap;
import java.util.Map;

import com.mongodb.reactivestreams.client.MongoCollection;

import org.bson.Document;

import pandorium.database.MongoSchema;
import pandorium.database.NonRequired;
import pandorium.database.Required;

public class PlayerInfo extends MongoSchema<String, Object> {
    public PlayerInfo(MongoCollection<Document> collection) {
        super(
            collection,
            new Required<>("uuid", String.class),
            new Required<>("token", String.class),
            new Required<>("banned", Boolean.class),
            new NonRequired<>("locale", String.class)
        );
    }

    public Document create(String uuid, String token, Boolean banned, String locale) {
        HashMap<String, Object> schema = new HashMap<>(Map.of(
                "uuid", uuid,
                "token", token,
                "banned", banned
        ));

        schema.put("locale", locale);
        return this.create(schema);
    }

    public Document create(String uuid, String token, Boolean banned) {
        return this.create(uuid, token, banned, null);
    }
}
