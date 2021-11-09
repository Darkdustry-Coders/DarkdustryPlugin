package pandorum.comp;

import arc.struct.IntMap;
import arc.util.Nullable;
import com.mongodb.BasicDBObject;
import mindustry.gen.Player;
import pandorum.models.PlayerModel;

import java.util.function.Consumer;

import static pandorum.Misc.bundled;

public class Ranks {

    public static Rank admin = new Rank("[accent]<[scarlet]\uE817[accent]> ", "[scarlet]Admin", null, null);
    public static Rank veteran = new Rank("[accent]<[white]\uE813[accent]> ", "[sky]Veteran", null, null);
    public static Rank active = new Rank("[accent]<[white]\uE800[accent]> ", "[cyan]Active", veteran, new Requirements(50000000L, 50000, 30));
    public static Rank player = new Rank("[accent]<> ", "[accent]Player", active, new Requirements(25000000L, 25000, 15));

    private static final IntMap<Rank> ranks = new IntMap<>();

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

    public static void init() {
        ranks.put(0, player);
        ranks.put(1, active);
        ranks.put(2, veteran);
        ranks.put(3, admin);
    }

    public static Rank get(int index) {
        return ranks.get(index);
    }

    public static int rankId(Rank rank) {
        return ranks.findKey(rank, false, 0);
    }

    public static void getRank(Player p, Consumer<Rank> callback) {
        PlayerModel.find(new BasicDBObject("UUID", p.uuid()), playerInfo -> {
            Rank current = get(playerInfo.rank);
            Rank next;
            if (p.admin) next = admin;
            else if (current.next != null && current.nextReq != null && current.nextReq.check(playerInfo.playTime, playerInfo.buildingsBuilt, playerInfo.gamesPlayed)) {
                next = current.next;
                bundled(p, "events.rank-increase", current.next.tag, current.next.name);
            } else if (current == admin) next = player;
            else next = current;

            if (playerInfo.rank != rankId(next)) {
                playerInfo.rank = rankId(next);
                playerInfo.save();
            }
            callback.accept(next);
        });
    }
}
