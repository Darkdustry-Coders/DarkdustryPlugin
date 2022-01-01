package pandorum.struct;

import arc.func.Boolf;
import arc.struct.Queue;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class Seqs {

    public static <T> SeqBuilder<T> seqBuilder() {
        return new SeqBuilder<>();
    }

    public static <T> SafeQueue<T> safeQueue() {
        return new SafeQueue<>();
    }

    public static <T> EmptyQueue<T> emptyQueue() {
        return new EmptyQueue<>();
    }

    private static class SafeQueue<T> extends Queue<T> {

        @Override
        public T removeFirst() {
            return isEmpty() ? null : super.removeFirst();
        }

        @Override
        public T removeLast() {
            return isEmpty() ? null : super.removeLast();
        }

        @Override
        public T removeIndex(int index) {
            return index < 0 || index >= size ? null : super.removeIndex(index);
        }

        @Override
        public T first() {
            return isEmpty() ? null : super.first();
        }

        @Override
        public T last() {
            return isEmpty() ? null : super.last();
        }

        @Override
        public T get(int index) {
            return index < 0 || index >= size ? null : super.get(index);
        }
    }

    private static class EmptyQueue<T> extends Queue<T> {

        @Override
        public void add(T object) {}

        @Override
        public void addFirst(T object) {}

        @Override
        public void addLast(T object) {}

        @Override
        public boolean remove(T value) {
            return false;
        }

        @Override
        public boolean remove(T value, boolean identity) {
            return false;
        }

        @Override
        public T removeIndex(int index) {
            return null;
        }

        @Override
        public T removeFirst() {
            return null;
        }

        @Override
        public T removeLast() {
            return null;
        }

        @Override
        public T first() {
            return null;
        }

        @Override
        public T last() {
            return null;
        }

        @Override
        public T get(int index) {
            return null;
        }

        @Override
        public int indexOf(T value, boolean identity) {
            return -1;
        }

        @Override
        public int indexOf(Boolf<T> value) {
            return -1;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    }

    public static class SeqBuilder<T> {
        protected long expireAfterWriteNanos = -1;
        protected int maximumSize = -1;

        public SeqBuilder<T> maximumSize(int maximumSize) {
            if (maximumSize < 0) throw new IllegalArgumentException("Maximum size must not be negative.");
            this.maximumSize = maximumSize;
            return this;
        }

        public SeqBuilder<T> expireAfterWrite(Duration duration) {
            return expireAfterWrite(toNanosSaturated(duration), TimeUnit.NANOSECONDS);
        }

        public SeqBuilder<T> expireAfterWrite(long duration, TimeUnit unit) {
            if (duration < 0) throw new IllegalArgumentException("Duration must not be negative.");
            this.expireAfterWriteNanos = unit.toNanos(duration);
            return this;
        }

        private long toNanosSaturated(Duration duration) {
            try {
                return duration.toNanos();
            } catch (ArithmeticException e) {
                return duration.isNegative() ? Long.MIN_VALUE : Long.MAX_VALUE;
            }
        }

        public <T1 extends T> CacheSeq<T1> build() {
            return new CacheSeq<>(this);
        }
    }
}
