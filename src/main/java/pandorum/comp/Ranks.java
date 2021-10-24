package pandorum.comp;

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

    public static class Rank {
        public String name;
        public String tag;
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

    public static void getRank(Player player, Consumer<Rank> callback) {
<<<<<<< HEAD:src/main/java/pandorum/comp/Ranks.java
        PlayerModel.find(
            PlayerModel.class,
            new BasicDBObject("UUID", player.uuid()),
            playerInfo -> {
                Rank rank;
                if (player.admin) rank = admin;
                else if (playerInfo.rank.next != null && playerInfo.rank.nextReq != null && playerInfo.rank.nextReq.check(playerInfo.playTime, playerInfo.buildingsBuilt, playerInfo.maxWave, playerInfo.gamesPlayed)) {
                    rank = playerInfo.rank.next;
                    bundled(player, "events.rank-increase", playerInfo.rank.next.tag, playerInfo.rank.next.name);
                } else rank = playerInfo.rank;

                playerInfo.rank = rank;
                callback.accept(rank);
            }
        );
=======
        PlayerModel.find(new BasicDBObject("UUID", player.uuid()), playerInfo -> {
            Rank rank;
            if (player.admin) rank = admin;
            else if (playerInfo.rank.next != null && playerInfo.rank.nextReq != null && playerInfo.rank.nextReq.check(playerInfo.playTime, playerInfo.buildingsBuilt, playerInfo.maxWave, playerInfo.gamesPlayed)) {
               rank = playerInfo.rank.next;
               //TODO сообщение о повышении ранга?
            } else rank = playerInfo.rank;

            playerInfo.rank = rank;
            callback.accept(rank);
        });
>>>>>>> parent of e485450 (tools):src/main/java/pandorum/ranks/Ranks.java
    }
}
