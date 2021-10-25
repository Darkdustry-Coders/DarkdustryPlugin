package pandorum.comp;

import arc.struct.IntMap;
import arc.util.Nullable;
import com.mongodb.BasicDBObject;
import mindustry.gen.Player;
import pandorum.models.PlayerModel;

import java.util.function.Consumer;

import static pandorum.Misc.bundled;

public class Ranks {

    public static Rank admin = new Rank("[accent]<[scarlet]\uE817[accent]> ", "Admin", null, null);
    public static Rank veteran = new Rank("[accent]<[white]\uE813[accent]> ", "Veteran", null, null);
    public static Rank active = new Rank("[accent]<[white]\uE800[accent]> ", "Active", veteran, new Requirements(45000000L, 30000, 250, 25));
    public static Rank player = new Rank("[accent]<> ", "Player", active, new Requirements(15000000L, 15000, 100, 10));

    public static IntMap<Rank> ranks = new IntMap<>();

    public static class Rank {
        public String tag;
        public String name;
        public @Nullable Rank next;
        public @Nullable Requirements nextReq;

        public Rank(String tag, String name, Rank next, Requirements nextReq) {
            this.name = name;
            this.tag = tag;
            this.next = next;
            this.nextReq = nextReq;
        }
    }

    public static class Requirements {
        public long playtime;
        public int buildingsBuilt;
        public int maxWave;
        public int gamesPlayed;

        public Requirements(long playtime, int buildingsBuilt, int maxWave, int gamesPlayed) {
            this.playtime = playtime;
            this.buildingsBuilt = buildingsBuilt;
            this.maxWave = maxWave;
            this.gamesPlayed = gamesPlayed;
        }

        public boolean check(long time, int built, int wave, int games) {
            return time >= playtime && built >= buildingsBuilt && wave >= maxWave && games >= gamesPlayed;
        }
    }

    public static void init() {
        ranks.put(0, player);
        ranks.put(1, active);
        ranks.put(2, veteran);
        ranks.put(3, admin);
    }

    public static void getRank(Player player, Consumer<Rank> callback) {
        PlayerModel.find(
            new BasicDBObject("UUID", player.uuid()),
            playerInfo -> {
                Rank current = ranks.get(playerInfo.rank);
                Rank rank;
                if (player.admin) rank = admin;
                else if (current.next != null && current.nextReq != null && current.nextReq.check(playerInfo.playTime, playerInfo.buildingsBuilt, playerInfo.maxWave, playerInfo.gamesPlayed)) {
                    rank = current.next;
                    bundled(player, "events.rank-increase", current.next.tag, current.next.name);
                } else rank = current;

                playerInfo.rank = ranks.findKey(rank, false, 0);
                callback.accept(rank);
            }
        );
    }
}
