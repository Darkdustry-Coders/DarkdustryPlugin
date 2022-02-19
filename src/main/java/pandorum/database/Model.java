package pandorum.database;

import arc.struct.ObjectMap;
import arc.util.Log;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.reactivestreams.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Model {

    public ObjectMap<String, Object> latest;

    public ObjectId _id;
    public int __v;

    public void save(MongoCollection<Document> collection) {
        ObjectMap<String, Object> values = getFields();
        BasicDBObject operations = DataChanges.toBsonOperations(latest, values);

        if (!operations.isEmpty()) {
            latest = values;
            collection.findOneAndUpdate(new BasicDBObject("_id", values.get("_id")), operations, (new FindOneAndUpdateOptions()).upsert(true).returnDocument(ReturnDocument.AFTER)).subscribe(new Subscriber<>() {
                public void onSubscribe(Subscription s) {
                    s.request(1);
                }

                public void onNext(Document t) {}

                public void onComplete() {}

                public void onError(Throwable t) {
                    Log.err(t);
                }
            });
        }
    }

    public ObjectMap<String, Object> getFields() {
        ObjectMap<String, Object> values = new ObjectMap<>();

        for (Field field : getClass().getFields()) {
            if (Modifier.isPublic(field.getModifiers())) {
                try {
                    values.put(field.getName(), field.get(this));
                } catch (Exception ignored) {}
            }
        }

        return values;
    }

    public void resetLatest() {
        this.latest = getFields();
    }
}
