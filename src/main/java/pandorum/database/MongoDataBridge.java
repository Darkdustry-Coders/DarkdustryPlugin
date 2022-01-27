package pandorum.database;

import arc.func.Cons;
import arc.struct.Seq;
import arc.util.Log;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.reactivestreams.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public abstract class MongoDataBridge<T extends MongoDataBridge<T>> {

    private static final Seq<String> specialKeys = Seq.with("_id", "__v", "DEFAULT_CODEC_REGISTRY");
    private static Map<String, Object> latest = new HashMap<>();

    public ObjectId _id;
    public int __v;

    public static <T extends MongoDataBridge<T>> void findAndApplySchema(MongoCollection<Document> collection, Class<T> sourceClass, Bson filter, Cons<T> cons) {
        try {
            T dataClass = sourceClass.getConstructor().newInstance();

            Seq<Field> fields = Seq.with(sourceClass.getFields());
            Document defaultObject = new Document();

            fields.each(field -> !specialKeys.contains(field.getName()), field -> {
                try {
                    defaultObject.append(field.getName(), field.get(dataClass));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    Log.err(e);
                }
            });

            filter.toBsonDocument().forEach(defaultObject::append);

            collection.findOneAndUpdate(filter,
                    new BasicDBObject("$setOnInsert", defaultObject),
                    new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
            ).subscribe(new Subscriber<>() {
                @Override
                public void onSubscribe(Subscription s) {
                    s.request(1);
                }

                @Override
                public void onNext(Document document) {
                    fields.each(field -> {
                        try {
                            field.set(dataClass, document.getOrDefault(field.getName(), field.get(dataClass)));
                        } catch (IllegalArgumentException | IllegalAccessException e) {
                            Log.err(e);
                        }
                    });

                    dataClass.resetLatest();
                    cons.get(dataClass);
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

    public void save(MongoCollection<Document> collection) {
        Map<String, Object> values = getDeclaredPublicFields();
        BasicDBObject operations = toBsonOperations(latest, values);

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

    public void resetLatest() {
        latest = getDeclaredPublicFields();
    }

    @Override
    public String toString() {
        return getDeclaredPublicFields().toString();
    }

    private Map<String, Object> getDeclaredPublicFields() {
        Field[] fields = getClass().getFields();
        Map<String, Object> values = new HashMap<>();

        for (Field field : fields) {
            if (Modifier.isPublic(field.getModifiers())) {
                try {
                    values.put(field.getName(), field.get(this));
                } catch (Exception ignored) {}
            }
        }

        return values;
    }

    private BasicDBObject toBsonOperations(Map<String, Object> previousFields, Map<String, Object> newFields) {
        Map<String, DataChanges> changes = DataChanges.getChanges(previousFields, newFields);
        Map<String, BasicDBObject> operations = new HashMap<>();

        changes.forEach((key, changedValues) -> {
            if (!changedValues.current.equals(DataChanges.undefined) && !specialKeys.contains(key)) {
                if (!operations.containsKey("$set")) operations.put("$set", new BasicDBObject());

                operations.get("$set").append(key, changedValues.current);
            }
        });

        return new BasicDBObject(operations);
    }
}
