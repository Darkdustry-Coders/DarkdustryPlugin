package pandorum.database;

import org.bson.types.Symbol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DataChanges {
    public static Symbol undefined = new Symbol("undefined");

    public Object previous;
    public Object current;

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
            Object firstValue = first.containsKey(key) ? first.get(key) : undefined;
            Object secondValue = second.containsKey(key) ? second.get(key) : undefined;

            if (firstValue == secondValue) return;

            changes.put(key, new DataChanges(firstValue, secondValue));
        });

        return changes;
    }
}
