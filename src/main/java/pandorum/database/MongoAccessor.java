package pandorum.database;

public class MongoAccessor<T> {
    private final Class<T> valueClass;
    private final String key;
    
    public MongoAccessor(String key, Class<T> valueClass) {
        this.key = key;
        this.valueClass = valueClass;
    }

    public Class<T> getDataClass() {
        return this.valueClass;
    }

    public String getKey() {
        return this.key;
    }

    public boolean isValidData(Class<?> valueClass) {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MongoAccessor<?> accessor
            && accessor.valueClass != null
            && accessor.key != null
            && accessor.key.equals(this.key)
            && accessor.valueClass == this.valueClass;
    }

    @Override
    public int hashCode() {
        return (this.key.hashCode() + this.valueClass.hashCode()) /2;
    }
}
