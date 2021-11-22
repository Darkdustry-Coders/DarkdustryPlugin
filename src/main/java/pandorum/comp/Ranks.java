package pandorum.comp;

import arc.struct.Seq;
import arc.util.Nullable;
import com.mongodb.BasicDBObject;
import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.models.PlayerModel;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static pandorum.Misc.findLocale;

public class Ranks {

    public static final Rank admin = new Rank("[accent]<[scarlet]\uE817[accent]> ", "[scarlet]Admin", 4, null, null);
    public static final Rank veteran = new Rank("[accent]<[gold]\uE809[accent]> ", "[gold]Veteran", 3, null, null);
    public static final Rank activePlus = new Rank("[accent]<[white]\uE813[accent]> ", "[sky]Active+", 2, veteran, new Requirements(100000000L, 100000, 100));
    public static final Rank active = new Rank("[accent]<[white]\uE800[accent]> ", "[cyan]Active", 1, activePlus, new Requirements(50000000L, 50000, 30));
    public static final Rank player = new Rank("[accent]<> ", "[accent]Player", 0, active, new Requirements(25000000L, 25000, 15));

    private static final Seq<Rank> ranks = new Seq<>(true);

    public static class Rank {
        public String tag;
        public String name;
        public int id;
        public @Nullable Rank next;
        public @Nullable Requirements nextReq;

        public Rank(String tag, String name, int id, Rank next, Requirements nextReq) {
            this.name = name;
            this.tag = tag;
            this.id = id;
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
        ranks.addAll(player, active, activePlus, veteran, admin);
    }

    public static Rank get(int index) {
        return ranks.get(index);
    }

    public static void getRank(Player p, Consumer<Rank> callback) {
        PlayerModel.find(new BasicDBObject("UUID", p.uuid()), playerInfo -> {
            Rank current = get(playerInfo.rank), next = current;
            if (p.admin) next = admin;
            else if (current.next != null && current.nextReq != null && current.nextReq.check(playerInfo.playTime, playerInfo.buildingsBuilt, playerInfo.gamesPlayed)) {
                next = current.next;

                Call.infoMessage(p.con, Bundle.format("events.rank-increase",
                        findLocale(p.locale),
                        next.tag,
                        next.name,
                        TimeUnit.MILLISECONDS.toMinutes(playerInfo.playTime),
                        playerInfo.buildingsBuilt,
                        playerInfo.gamesPlayed)
                );

            } else if (current == admin) next = player;

            if (playerInfo.rank != next.id) {
                playerInfo.rank = next.id;
                playerInfo.save();
            }

            callback.accept(next);
        });
    }
}
