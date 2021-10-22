package pandorum.ranks;

import java.util.function.Consumer;

import com.mongodb.BasicDBObject;

import arc.struct.IntMap;
import mindustry.gen.Player;
import pandorum.models.PlayerModel;

public class Ranks {
    public static IntMap<Rank> rankNames = new IntMap<>();

    public static class Rank {
        public String name;
        public String tag;

        public Rank(String tag, String name) {
            this.name = name;
            this.tag = tag;
        }
    }

    public static void init() {
        rankNames.put(0, new Rank("[accent]<> ", "Player"));
        rankNames.put(1, new Rank("[accent]<[white]\uE800[accent]> ", "Active"));
        rankNames.put(2, new Rank("[accent]<[white]\uE813[accent]> ", "Veteran"));
        rankNames.put(3, new Rank("[accent]<[scarlet]\uE817[accent]> ", "Admin"));
    }

    public static class activeReq {
        // Миллисекунды
        public static long playtime = 750 * 60 * 1000;
        public static int buildingsBuilt = 1500 * 10;
        public static int maxWave = 100;
        public static int gamesPlayed = 10;

        public static boolean check(long playtime, int buildingsBuilt, int maxWave, int gamesPlayed) {
            return playtime >= activeReq.playtime && buildingsBuilt >= activeReq.buildingsBuilt && maxWave >= activeReq.maxWave && gamesPlayed >= activeReq.gamesPlayed;
        }
    }

    public static class veteranReq {
        // Миллисекунды
        public static long playtime = 1500 * 60 * 1000;
        public static int buildingsBuilt = 2500 * 10;
        public static int maxWave = 250;
        public static int gamesPlayed = 25;

        public static boolean check(long playtime, int buildingsBuilt, int maxWave, int gamesPlayed) {
            return playtime >= veteranReq.playtime && buildingsBuilt >= veteranReq.buildingsBuilt && maxWave >= veteranReq.maxWave && gamesPlayed >= veteranReq.gamesPlayed;
        }
    }

    public static void updateRank(Player player, Consumer<Rank> callback) {
        PlayerModel.find(new BasicDBObject("UUID", player.uuid()), playerInfo -> {
            Rank rank;
            if (player.admin) rank = rankNames.get(3);
            else if (veteranReq.check(playerInfo.playTime, playerInfo.buildingsBuilt, playerInfo.maxWave, playerInfo.gamesPlayed)) rank = rankNames.get(2);
            else if (activeReq.check(playerInfo.playTime, playerInfo.buildingsBuilt, playerInfo.maxWave, playerInfo.gamesPlayed)) rank = rankNames.get(1);
            else rank = rankNames.get(0);

            callback.accept(rank);
        });
    }
}