package pandorum.features;

import arc.struct.Seq;
import pandorum.data.PlayerData;

import static pandorum.data.Database.getPlayerData;
import static pandorum.data.Database.setPlayerData;

public class Ranks {

    public static Rank player, active, activePlus, veteran, contributor, admin;

    // TODO упростить загрузку рангов?
    public static void load() {
        player = new Rank() {{
            tag = "[accent]<[white]\uE800[]>[] ";
            name = "player";
            displayName = "[accent]Player";

            next = active = new Rank() {{
                tag = "[accent]<[white]\uE800[]>[] ";
                name = "active";
                displayName = "[sky]Active";
                req = new Requirements(300 * 60, 25000, 20);

                next = activePlus = new Rank() {{
                    tag = "[accent]<[white]\uE813[]>[] ";
                    name = "active+";
                    displayName = "[cyan]Active+";
                    req = new Requirements(750 * 60, 50000, 40);

                    next = veteran = new Rank() {{
                        tag = "[accent]<[gold]\uE809[]>[] ";
                        name = "veteran";
                        displayName = "[gold]Veteran";
                        req = new Requirements(1500 * 60, 100000, 100);
                    }};
                }};
            }};
        }};

        contributor = new Rank() {{
            tag = "[accent]<[yellow]\uE80F[]>[] ";
            name = "contributor";
            displayName = "[yellow]Contributor";
        }};

        admin = new Rank() {{
            tag = "[accent]<[scarlet]\uE817[]>[] ";
            name = "admin";
            displayName = "[scarlet]Admin";
        }};
    }

    public static Rank getRank(int id) {
        return Rank.ranks.get(id);
    }

    // TODO вот что это нахер
    public static void setRank(String uuid, Rank rank) {
        PlayerData data = getPlayerData(uuid);
        if (data == null) return;

        data.rank = rank.id;

        if (rank.req != null) {
            data.playTime = rank.req.playTime;
            data.buildingsBuilt = rank.req.buildingsBuilt;
            data.gamesPlayed = rank.req.gamesPlayed;
        }

        setPlayerData(uuid, data);
    }

    public static class Rank {
        public static final Seq<Rank> ranks = new Seq<>();

        public String tag = "";
        public String name = "";
        public String displayName = "";

        public int id;

        public Requirements req = null;
        public Rank next = null;

        public Rank() {
            this.id = ranks.size;
            ranks.add(this);
        }

        public boolean checkNext(int playTime, int buildingsBuilt, int gamesPlayed) {
            return next != null && next.req != null && next.req.check(playTime, buildingsBuilt, gamesPlayed);
        }
    }

    public record Requirements(int playTime, int buildingsBuilt, int gamesPlayed) {
        public boolean check(int playTime, int buildingsBuilt, int gamesPlayed) {
            return playTime >= this.playTime && buildingsBuilt >= this.buildingsBuilt && gamesPlayed >= this.gamesPlayed;
        }
    }
}
