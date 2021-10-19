package pandorum.commands.client;

import mindustry.gen.Call;
import mindustry.gen.Player;
import org.bson.Document;
import pandorum.Misc;
import pandorum.comp.Bundle;
import pandorum.ranks.RankType;
import pandorum.ranks.Ranker;

import java.util.concurrent.TimeUnit;

import static pandorum.PandorumPlugin.createInfo;
import static pandorum.PandorumPlugin.savePlayerStats;

public class RankCommand {
    public static void run(final String[] args, final Player player) {
        Document playerInfo = createInfo(player);
        savePlayerStats(player.uuid());

        String message = player.admin ? Bundle.format("commands.rank.admin", Misc.findLocale(player.locale)) : switch(RankType.getByNumber(playerInfo.getInteger("rank"))) {
            case player -> Bundle.format("commands.rank.player", Misc.findLocale(player.locale), TimeUnit.MILLISECONDS.toMinutes(playerInfo.getLong("playtime")), Ranker.activePlaytime, playerInfo.getInteger("wavesSurvived"), Ranker.activeWavesSurvived, playerInfo.getInteger("gamesWon"), Ranker.activeGamesWon, playerInfo.getLong("buildingsBuilt"), Ranker.activeBuildingsBuilt);
            case active -> Bundle.format("commands.rank.active", Misc.findLocale(player.locale), TimeUnit.MILLISECONDS.toMinutes(playerInfo.getLong("playtime")), Ranker.veteranPlaytime, playerInfo.getInteger("wavesSurvived"), Ranker.veteranWavesSurvived, playerInfo.getInteger("gamesWon"), Ranker.veteranGamesWon, playerInfo.getLong("buildingsBuilt"), Ranker.veteranBuildingsBuilt);
            case veteran -> Bundle.format("commands.rank.veteran", Misc.findLocale(player.locale));
        };

        Call.infoMessage(player.con, message);
    }
}
