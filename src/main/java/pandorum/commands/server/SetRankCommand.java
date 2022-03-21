package pandorum.commands.server;

import arc.func.Cons;
import arc.util.Log;
import mindustry.gen.Player;
import pandorum.components.Ranks;
import pandorum.components.Ranks.Rank;

import static pandorum.util.Search.findPlayer;
import static pandorum.util.Search.findRank;

public class SetRankCommand implements Cons<String[]> {
    public void get(String[] args) {
        Rank rank = findRank(args[0]);
        if (rank == null) {
            Log.err("Ранг '@' не найден...", args[0]);
            return;
        }

        Player target = findPlayer(args[1]);
        if (target == null) {
            Log.err("Игрок '@' не найден...", args[1]);
            return;
        }

        Ranks.setRank(target.uuid(), rank);
        Log.info("Ранг игрока '@' успешно изменен на '@'", target.name, rank.name);
    }
}
