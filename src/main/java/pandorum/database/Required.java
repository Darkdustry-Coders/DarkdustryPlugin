package pandorum.database;

public class Required<T> extends MongoAccessor<T> {
    public Required(String key, Class<T> valueClass) {
        super(key, valueClass);
    }

    @Override
    public boolean isValidData(Class<?> valueClass) {
        return valueClass == super.getDataClass();
    }
}
