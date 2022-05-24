package pandorum.commands.server;

import arc.func.Cons;
import arc.util.Log;
import mindustry.net.Administration.PlayerInfo;
import pandorum.features.Ranks;
import pandorum.features.Ranks.Rank;

import static pandorum.util.Search.*;

public class SetRankCommand implements Cons<String[]> {
    public void get(String[] args) {
        Rank rank = findRank(args[0]);
        if (rank == null) {
            Log.err("Ранг '@' не найден...", args[0]);
            return;
        }

        PlayerInfo info = findPlayerInfo(args[1]);
        if (info == null) {
            Log.err("Игрок '@' не найден...", args[1]);
            return;
        }

        Ranks.setRank(info.id, rank);
        Log.info("Ранг игрока '@' успешно изменен на '@'", info.lastName, rank.name);
    }
}
