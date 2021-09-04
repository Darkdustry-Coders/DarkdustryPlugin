package pandorium.database;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@SuppressWarnings("unchecked")
public class MongoEvents {
    public Map<Class<?>, HashSet<Callback<Object>>> listeners = new HashMap<>();

    public <T> void addListener(Class<?> event, Callback<T> listener) {
        listeners.computeIfAbsent(event, k -> new HashSet<>());
        listeners.get(event).add((Callback<Object>) listener);
    }

    public void fireEvent(Class<?> event, Object... callParams) {
        if (!listeners.containsKey(event)) return;
        listeners.get(event).forEach((listener) -> {
            try {
                listener.call(event.getDeclaredConstructors()[0].newInstance(callParams));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}