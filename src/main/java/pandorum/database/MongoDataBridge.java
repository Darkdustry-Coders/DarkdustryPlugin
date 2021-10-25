package pandorum.database;

import arc.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.function.Consumer;

import com.mongodb.BasicDBObject;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.reactivestreams.client.MongoCollection;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public abstract class MongoDataBridge<T extends MongoDataBridge<T>> {
    private static MongoCollection<Document> collection;
    private static final Set<String> specialKeys = Set.of(
        "_id", "__v", "DEFAULT_CODEC_REGISTRY"
    );

    public ObjectId _id;
    public int __v;
    private Map<String, Object> latest = new HashMap<>();

    public static void setSourceCollection(MongoCollection<Document> collection) {
        MongoDataBridge.collection = collection;
    }

    public void save(Consumer<Throwable> callback) {
        Map<String, Object> values = getDeclaredPublicFields();

        BasicDBObject operations = toBsonOperations(latest, values);
        
        if (!operations.isEmpty())
            latest = values;
        collection.findOneAndUpdate(
            new BasicDBObject("_id", values.get("_id")),
            operations,
            new FindOneAndUpdateOptions()
                .upsert(true)
                .returnDocument(ReturnDocument.AFTER)
        ).subscribe(new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1);
            }

            @Override
            public void onNext(Document t) {
            }

            @Override
            public void onComplete() {
                callback.accept(null);
            }

            @Override
            public void onError(Throwable t) {
                if (!Objects.isNull(t)) Log.err(t);
                callback.accept(t);
            }
        });
    }

    public void save() {
        save(Throwable::printStackTrace);
    }

    public static <T extends MongoDataBridge<T>>void findAndApplySchema(Class<T> clazz, Bson filter, Consumer<T> callback) {
        try {
            T dataClass = clazz.getConstructor().newInstance();

            Set<Field> fields = Set.of(clazz.getFields());
            Document defaultObject = new Document();

            fields.forEach((field) -> {
                if (!specialKeys.contains(field.getName()))
                    try {
                        defaultObject.append(
                            field.getName(),
                            field.get(dataClass)
                        );
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
            });

            filter.toBsonDocument().forEach(defaultObject::append);

            collection
                .findOneAndUpdate(
                    filter,
                    new BasicDBObject(
                        "$setOnInsert",
                        defaultObject
                    ),
                    new FindOneAndUpdateOptions()
                        .upsert(true)
                        .returnDocument(ReturnDocument.AFTER)
                ).subscribe(new Subscriber<>() {
                        @Override
                        public void onSubscribe(Subscription s) {
                            s.request(1);
                        }

                        @Override
                        public void onNext(Document document) {
                            fields.forEach((field) -> {
                                try {
                                    field.set(
                                            dataClass,
                                            document.getOrDefault(field.getName(), field.get(dataClass))
                                    );
                                } catch (IllegalArgumentException | IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                            });

                            dataClass.resetLatest();
                            callback.accept(dataClass);
                        }

                        @Override
                        public void onComplete() {
                        }

                        @Override
                        public void onError(Throwable t) {
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetLatest() {
        this.latest = getDeclaredPublicFields();
    }

    @Override
    public String toString() {
        return getDeclaredPublicFields().toString();
    }


    private Map<String, Object> getDeclaredPublicFields() {
        Field[] fields = this.getClass().getFields();
        
        Map<String, Object> values = new HashMap<>();

        for (Field field : fields) {
            if (!Modifier.isPublic(field.getModifiers()))
                continue;
            try {
                values.put(
                    field.getName(),
                    field.get(this)
                );
            } catch (Exception ignored) {}
        }

        return values;
    }

    private BasicDBObject toBsonOperations(
        Map<String, Object> previousFields,
        Map<String, Object> newFields
    ) {
        Map<String, DataChanges> changes = DataChanges.getChanges(previousFields, newFields);
        Map<String, BasicDBObject> operations = new HashMap<>();

        changes.forEach((key, changedValues) -> {
            if (
                changedValues.current.equals(DataChanges.undefined)
                || specialKeys.contains(key)
            ) return;

            if (!operations.containsKey("$set"))
                operations.put("$set", new BasicDBObject());
            operations.get("$set").append(key, changedValues.current);
        });

        return new BasicDBObject(operations);
    }
}
