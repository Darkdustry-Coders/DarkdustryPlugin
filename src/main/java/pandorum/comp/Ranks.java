package pandorum.comp;

import arc.func.Cons;
import arc.struct.Seq;
import arc.util.Nullable;
import com.mongodb.BasicDBObject;
import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.models.PlayerModel;

import java.util.concurrent.TimeUnit;

import static pandorum.Misc.findLocale;

public class Ranks {
    public static final Rank admin = new Rank("[accent]<[scarlet]\uE817[accent]> ", "[scarlet]Admin", null, null);
    public static final Rank veteran = new Rank("[accent]<[gold]\uE809[accent]> ", "[gold]Veteran", new Requirements(100000000L, 100000, 100), null);
    public static final Rank activePlus = new Rank("[accent]<[white]\uE813[accent]> ", "[sky]Active+", new Requirements(50000000L, 50000, 30), veteran);
    public static final Rank active = new Rank("[accent]<[white]\uE800[accent]> ", "[cyan]Active", new Requirements(25000000L, 25000, 15), activePlus);
    public static final Rank player = new Rank("", "[accent]Player", null, active);

    private static final Seq<Rank> ranks = Seq.with(player, active, activePlus, veteran, admin);

    public static class Rank {
        public String tag;
        public String name;
        public @Nullable Rank next;
        public @Nullable Requirements req;

        public Rank(String tag, String name, Requirements req, Rank next) {
            this.name = name;
            this.tag = tag;
            this.req = req;
            this.next = next;
        }
    }

    public static class Requirements {
        public long playtime;
        public int buildingsBuilt;
        public int gamesPlayed;

        public Requirements(long playtime, int buildingsBuilt, int gamesPlayed) {
            this.playtime = playtime;
            this.buildingsBuilt = buildingsBuilt;
            this.gamesPlayed = gamesPlayed;
        }

        public boolean check(long time, int built, int games) {
            return time >= playtime && built >= buildingsBuilt && games >= gamesPlayed;
        }
    }

    public static Rank getBestRank(long time, int built, int games) {
        Rank best = player;
        for (Rank rank : ranks) {
            if (rank.req != null && rank.req.check(time, built, games)) best = rank;
        }
        return best;
    }

    public static void updateRank(Player player, Cons<Rank> callback) {
        PlayerModel.find(new BasicDBObject("UUID", player.uuid()), playerInfo -> {
            if (player.admin && playerInfo.rank != admin) {
                playerInfo.rank = admin;
                playerInfo.save();
                callback.get(playerInfo.rank);
                return;
            }

            if (!player.admin && playerInfo.rank == admin) {
                playerInfo.rank = getBestRank(playerInfo.playTime, playerInfo.buildingsBuilt, playerInfo.gamesPlayed);
                playerInfo.save();
                callback.get(playerInfo.rank);
                return;
            }

            if (playerInfo.rank.next != null && playerInfo.rank.next.req != null && playerInfo.rank.next.req.check(playerInfo.playTime, playerInfo.buildingsBuilt, playerInfo.gamesPlayed)) {
                Call.infoMessage(player.con, Bundle.format("events.rank-increase",
                        findLocale(player.locale),
                        playerInfo.rank.next.tag,
                        playerInfo.rank.next.name,
                        TimeUnit.MILLISECONDS.toMinutes(playerInfo.playTime),
                        playerInfo.buildingsBuilt,
                        playerInfo.gamesPlayed
                ));

                playerInfo.rank = playerInfo.rank.next;
                playerInfo.save();
                callback.get(playerInfo.rank.next);
                return;
            }

            callback.get(playerInfo.rank);
        });
    }
}
