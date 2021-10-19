package pandorum.commands.server;

import arc.util.Log;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import org.bson.Document;
import pandorum.Misc;
import pandorum.PandorumPlugin;
import pandorum.ranks.RankType;

public class ModeratorCommand {
    public static void run(final String[] args) {
        if (!args[0].equalsIgnoreCase("add") && !args[0].equalsIgnoreCase("remove")) {
            Log.err("Используй add или remove в качестве первого аргумента!");
            return;
        }

        Document playerInfo = PandorumPlugin.playersInfo.find((info) -> info.getString("uuid").equals(args[1]));
        if (playerInfo == null) {
            Log.err("Игрок не найден!");
            return;
        }

        Player player = Groups.player.find(p -> p.uuid().equals(args[1]));

        if (args[0].equalsIgnoreCase("add")) {
            if (playerInfo.getInteger("permission") < RankType.moderator.permission) {
                playerInfo.replace("permission", RankType.moderator.permission);
                PandorumPlugin.savePlayerStats(args[1]);
                Log.info("Игрок успешно повышен!");
                if (player != null) Misc.bundled(player, "events.moderator-promotion");
                return;
            }
            Log.err("Игрок уже модератор!");
        }
        else if (args[0].equalsIgnoreCase("remove")) {
            if (playerInfo.getInteger("permission") >= RankType.moderator.permission) {
                playerInfo.replace("permission", RankType.player.permission);
                PandorumPlugin.savePlayerStats(args[1]);
                Log.info("Игрок успешно понижен!");
                if (player != null) Misc.bundled(player, "events.downgrade");
                return;
            }
            Log.err("Игрок не модератор!");
        }
    }
}
