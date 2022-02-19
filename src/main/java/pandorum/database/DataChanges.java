package pandorum.database;

import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import com.mongodb.BasicDBObject;
import org.bson.types.Symbol;

import java.util.HashMap;
import java.util.Map;

import static pandorum.PluginVars.specialKeys;

public class DataChanges {

    public static final Symbol undefined = new Symbol("undefined");

    public final Object previous;
    public final Object current;

    public DataChanges(Object previous, Object current) {
        this.previous = previous;
        this.current = current;
    }

    public static ObjectMap<String, DataChanges> getChanges(ObjectMap<String, Object> first, ObjectMap<String, Object> second) {
        ObjectSet<String> keys = new ObjectSet<>();
        ObjectMap<String, DataChanges> changes = new ObjectMap<>();

        keys.addAll(first.keys().toSeq());
        keys.addAll(second.keys().toSeq());

        keys.forEach(key -> {
            Object firstValue = first.get(key, undefined);
            Object secondValue = second.get(key, undefined);

            if (firstValue == secondValue) return;

            changes.put(key, new DataChanges(firstValue, secondValue));
        });

        return changes;
    }

    public static BasicDBObject toBsonOperations(ObjectMap<String, Object> previousFields, ObjectMap<String, Object> newFields) {
        ObjectMap<String, DataChanges> changes = getChanges(previousFields, newFields);
        Map<String, BasicDBObject> operations = new HashMap<>();

        changes.each((key, value) -> {
            if (!value.current.equals(DataChanges.undefined) && !specialKeys.contains(key)) {
                if (!operations.containsKey("$set")) operations.put("$set", new BasicDBObject());

                operations.get("$set").append(key, value.current);
            }
        });

        return new BasicDBObject(operations);
    }
}
