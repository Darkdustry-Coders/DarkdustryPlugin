package pandorum.entry;

import java.util.concurrent.TimeUnit;
import mindustry.gen.*;

public interface HistoryEntry{

    String getMessage(Player player);

    long getLastAccessTime(TimeUnit unit);
}
