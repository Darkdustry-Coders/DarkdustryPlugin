package pandorum.database;

import com.mongodb.BasicDBObject;
import org.bson.types.Symbol;

import java.util.HashMap;
import java.util.HashSet;
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

    public static Map<String, DataChanges> getChanges(Map<String, Object> first, Map<String, Object> second) {
        HashSet<String> keys = new HashSet<>();
        HashMap<String, DataChanges> changes = new HashMap<>();

        keys.addAll(first.keySet());
        keys.addAll(second.keySet());

        keys.forEach(key -> {
            Object firstValue = first.getOrDefault(key, undefined);
            Object secondValue = second.getOrDefault(key, undefined);

            if (firstValue == secondValue) return;

            changes.put(key, new DataChanges(firstValue, secondValue));
        });

        return changes;
    }

    public static BasicDBObject toBsonOperations(Map<String, Object> previousFields, Map<String, Object> newFields) {
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
