package pandorum.struct;

import java.util.Objects;

public class Tuple2<T1, T2> {

    public final T1 t1;
    public final T2 t2;

    public Tuple2(T1 t1, T2 t2) {
        this.t1 = t1;
        this.t2 = t2;
    }

    public static <T1, T2> Tuple2<T1, T2> of(T1 t1, T2 t2) {
        return new Tuple2<>(t1, t2);
    }

    public Object get(int index) {
        return switch (index) {
            case 0 -> t1;
            case 1 -> t2;
            default -> null;
        };
    }

    public int size() {
        return 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof Tuple2<?, ?> tuple2 && t1.equals(tuple2.t1) && t2.equals(tuple2.t2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(t1, t2);
    }
}