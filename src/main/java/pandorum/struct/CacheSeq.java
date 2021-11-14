package pandorum.struct;

import arc.struct.*;
import arc.util.Time;

import java.util.Objects;

public class CacheSeq<T> extends Seq<T> {
    protected static final int UNSET_INT = -1;

    private final Queue<Tuple2<T, Long>> writeQueue;
    private final long expireAfterWriteNanos;
    private final int maximumSize;

    CacheSeq(Seqs.SeqBuilder<? super T> builder) {
        maximumSize = builder.maximumSize;
        expireAfterWriteNanos = builder.expireAfterWriteNanos;
        writeQueue = expiresAfterWrite() ? Seqs.safeQueue() : Seqs.emptyQueue();
    }

    @Override
    public void add(T e) {
        if (e == null) {
            return;
        }

        super.add(e);
        writeQueue.add(Tuple2.of(e, Time.nanos()));

        cleanUpBySize();
        cleanUp();
    }

    @Override
    public T get(int index) {
        try {
            return super.get(index);
        } finally {
            cleanUp();
        }
    }

    @Override
    public T peek() {
        try {
            return isEmpty() ? null : super.peek();
        } finally {
            cleanUp();
        }
    }

    @Override
    public T first() {
        try {
            return isEmpty() ? null : super.first();
        } finally {
            cleanUp();
        }
    }

    @Override
    public boolean remove(T value) {
        int index = writeQueue.indexOf(t -> Objects.equals(t.t1, value));
        if (index != -1) {
            writeQueue.removeIndex(index);
        }
        return super.remove(value);
    }

    public boolean isOverflown() {
        return evictsBySize() && size >= maximumSize;
    }

    public boolean expiresAfterWrite() {
        return expireAfterWriteNanos > 0;
    }

    public boolean evictsBySize() {
        return maximumSize >= 0;
    }

    public void cleanUp() {
        Tuple2<T, Long> t;
        while ((t = writeQueue.last()) != null && isExpired(t.t2)) {
            remove(t.t1);
        }
    }

    public void cleanUpBySize() {
        if (!evictsBySize()) return;

        while (size > maximumSize) {
            remove(first());
        }
    }

    private boolean isExpired(Long time) {
        return expiresAfterWrite() && time != null && Time.timeSinceNanos(time) >= expireAfterWriteNanos;
    }
}
