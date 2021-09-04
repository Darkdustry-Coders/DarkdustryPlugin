package pandorum.entry;

import mindustry.gen.Player;

public interface HistoryEntry{

    String getMessage(Player player);
 
    //TODO сделать отображение времени, когда был изменён блок
}
