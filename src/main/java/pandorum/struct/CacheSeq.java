package pandorum.struct;

import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Time;

import java.util.Objects;

public class CacheSeq<T> extends Seq<T> {

    private final int maximumSize;
    private final long expireAfterWriteNanos;
    private final Queue<Tuple2<T, Long>> writeQueue;

    public CacheSeq(Seqs.SeqBuilder<? super T> builder) {
        this.maximumSize = builder.maximumSize;
        this.expireAfterWriteNanos = builder.expireAfterWriteNanos;
        this.writeQueue = expiresAfterWrite() ? Seqs.safeQueue() : Seqs.emptyQueue();
    }

    @Override
    public void add(T value) {
        if (value == null) return;

        super.add(value);
        writeQueue.add(Tuple2.of(value, Time.nanos()));

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
        writeQueue.remove(t -> Objects.equals(t.t1, value));
        return super.remove(value);
    }

    public boolean isOverflown() {
        return evictsBySize() && size >= maximumSize;
    }

    public boolean expiresAfterWrite() {
        return expireAfterWriteNanos > 0;
    }

    public boolean evictsBySize() {
        return maximumSize > 0;
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
