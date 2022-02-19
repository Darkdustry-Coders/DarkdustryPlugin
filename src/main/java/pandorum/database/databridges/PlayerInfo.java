package pandorum.database.databridges;

import arc.func.Cons;
import arc.struct.Seq;
import arc.util.Log;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.reactivestreams.client.MongoCollection;
import mindustry.gen.Player;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import pandorum.database.Model;

import java.lang.reflect.Field;

import static pandorum.PluginVars.specialKeys;

public class PlayerInfo {

    public static final Class<PlayerModel> modelClass = PlayerModel.class;
    public static MongoCollection<Document> collection;

    public static void find(Player player, Cons<PlayerModel> cons) {
        if (player != null) find(player.uuid(), cons);
    }

    public static void find(String UUID, Cons<PlayerModel> cons) {
        try {
            PlayerModel model = modelClass.getConstructor().newInstance();

            Seq<Field> fields = Seq.with(modelClass.getFields());
            Document defaultObject = new Document();
            Bson filter = new BasicDBObject("UUID", UUID);

            fields.each(field -> !specialKeys.contains(field.getName()), field -> {
                try {
                    defaultObject.append(field.getName(), field.get(model));
                } catch (Exception e) {
                    Log.err(e);
                }
            });

            filter.toBsonDocument().forEach(defaultObject::append);

            collection.findOneAndUpdate(filter, new BasicDBObject("$setOnInsert", defaultObject), new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)).subscribe(new Subscriber<>() {
                @Override
                public void onSubscribe(Subscription s) {
                    s.request(1);
                }

                @Override
                public void onNext(Document document) {
                    fields.each(field -> {
                        try {
                            field.set(model, document.getOrDefault(field.getName(), field.get(model)));
                        } catch (Exception e) {
                            Log.err(e);
                        }
                    });

                    cons.get(model);
                }

                @Override
                public void onComplete() {}

                @Override
                public void onError(Throwable t) {
                    Log.err(t);
                }
            });
        } catch (Exception e) {
            Log.err(e);
        }
    }

    public static void save(PlayerModel model) {
        model.save(collection);
    }

    public static class PlayerModel extends Model {

        public String UUID;

        public String locale = "off";
        public boolean welcomeMessage = true;
        public boolean alerts = true;
        public int playTime = 0;
        public int buildingsBuilt = 0;
        public int buildingsDeconstructed = 0;
        public int gamesPlayed = 0;
        public int rank = 0;
    }
}
