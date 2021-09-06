package pandorum.models;

import java.util.HashMap;
import java.util.Map;

import com.mongodb.reactivestreams.client.MongoCollection;

import org.bson.Document;

import pandorum.database.MongoSchema;
import pandorum.database.NonRequired;
import pandorum.database.Required;

public class PlayerInfo extends MongoSchema<String, Object> {
    public static PlayerInfo(MongoCollection<Document> collection) {
        super(
            collection,
            new Required<>("uuid", String.class),
            new Required<>("token", String.class),
            new Required<>("banned", Boolean.class),
            new NonRequired<>("locale", String.class)
        );
    }

    public static Document create(String uuid, String token, Boolean banned, String locale) {
        HashMap<String, Object> schema = new HashMap<>(Map.of(
                "uuid", uuid,
                "token", token,
                "banned", banned
        ));

        schema.put("locale", locale);
        return this.create(schema);
    }

    public static Document create(String uuid, String token, Boolean banned) {
        return this.create(uuid, token, banned, null);
    }
}
