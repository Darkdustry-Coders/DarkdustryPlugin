package pandorum.struct;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Iterator;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

public class Tuple2<T1, T2> implements Iterable<Object>, Serializable {
    @Serial
    private static final long serialVersionUID = -2157474721147356218L;

    public final T1 t1;
    public final T2 t2;

    Tuple2(T1 t1, T2 t2) {
        this.t1 = Objects.requireNonNull(t1, "t1");
        this.t2 = Objects.requireNonNull(t2, "t2");
    }

    public static <T1, T2> Tuple2<T1, T2> of(T1 t1, T2 t2) {
        return new Tuple2<>(t1, t2);
    }

    public int size() {
        return 2;
    }

    public Object get(int index) {
        return switch (index) {
            case 0 -> t1;
            case 1 -> t2;
            default -> null;
        };
    }

    public List<Object> toList() {
        return Arrays.asList(toArray());
    }

    public Object[] toArray() {
        return new Object[]{t1, t2};
    }

    @Override
    public Iterator<Object> iterator() {
        return Collections.unmodifiableList(toList()).iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>)o;
        return t1.equals(tuple2.t1) && t2.equals(tuple2.t2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(t1, t2);
    }
}


