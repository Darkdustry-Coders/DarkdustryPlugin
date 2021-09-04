package pandorum.database;

public class NonRequired<T> extends MongoAccessor<T> {
    private final T defaultValue;

    public NonRequired(String key, Class<T> valueClass) {
        this(key, valueClass, null);
    }

    public NonRequired(String key, Class<T> valueClass, T defaultValue) {
        super(key, valueClass);
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean isValidData(Class<?> valueClass) {
        return true;
    }

    public T getDefaultValue() {
        return this.defaultValue;
    }

    public boolean hasDefault() {
        return null != this.getDefaultValue();
    }
}
