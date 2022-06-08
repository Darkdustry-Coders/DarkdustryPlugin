package pandorum.features.history;

import arc.struct.Seq;
import pandorum.features.history.entry.HistoryEntry;

public class HistorySeq extends Seq<HistoryEntry> {

    public final int maxSize;

    public HistorySeq(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public void add(HistoryEntry entry) {
        super.add(entry);

        while(size > maxSize) {
            remove(first());
        }
    }
}
