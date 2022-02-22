package pandorum.components;

import arc.struct.Seq;
import arc.util.Strings;
import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.database.models.PlayerModel;

import static pandorum.util.Search.findLocale;
import static pandorum.util.Utils.secondsToMinutes;

public class Ranks {

    public static Rank player;
    public static Rank active;
    public static Rank activePlus;
    public static Rank veteran;
    public static Rank contributor;
    public static Rank admin;

    public static void init() {
        player = new Rank("", "player", "[accent]Player", null, active);
        active = new Rank("[accent]<[white]\uE800[accent]> ", "active", "[sky]Active", new Requirements(300 * 60, 25000, 20), activePlus);
        activePlus = new Rank("[accent]<[white]\uE813[accent]> ", "active+", "[cyan]Active+", new Requirements(750 * 60, 50000, 40), veteran);
        veteran = new Rank("[accent]<[gold]\uE809[accent]> ", "veteran", "[gold]Veteran", new Requirements(1500 * 60, 100000, 100), null);
        contributor = new Rank("[accent]<[lime]\uE80F[accent]>", "contributor", "[lime]Contributor");
        admin = new Rank("[accent]<[scarlet]\uE817[accent]> ", "admin", "[scarlet]Admin");
    }

    public static Rank getRank(int index) {
        return Rank.ranks.get(index);
    }

    public static Rank findRank(String name) {
        return Strings.canParsePositiveInt(name) ? Rank.ranks.get(Strings.parseInt(name)) : Rank.ranks.find(rank -> rank.name.equalsIgnoreCase(name));
    }

    public static void updateRank(Player player) {
        PlayerModel.find(player, playerModel -> {
            Rank current = getRank(playerModel.rank);

            if (current.next != null && current.next.req != null && current.next.req.check(playerModel.playTime, playerModel.buildingsBuilt, playerModel.gamesPlayed)) {
                Call.infoMessage(player.con, Bundle.format("events.rank-increase",
                        findLocale(player.locale),
                        current.next.tag,
                        current.next.name,
                        secondsToMinutes(playerModel.playTime),
                        playerModel.buildingsBuilt,
                        playerModel.gamesPlayed
                ));

                playerModel.rank = current.next.id;
                playerModel.save();
                player.name(current.next.tag + "[#" + player.color.toString() + "]" + player.getInfo().lastName);
                return;
            }

            if (player.admin && current != admin) {
                current = admin;
                playerModel.rank = current.id;
                playerModel.save();
            }

            player.name(current.tag + "[#" + player.color.toString() + "]" + player.getInfo().lastName);
        });
    }

    public static class Rank {
        public static final Seq<Rank> ranks = new Seq<>(true);

        public final String tag;
        public final String name;
        public final String displayName;
        public final int id;
        public final Rank next;
        public final Requirements req;

        public Rank(String tag, String name, String displayName, Requirements req, Rank next) {
            this.tag = tag;
            this.name = name;
            this.displayName = displayName;
            this.id = ranks.size;
            this.req = req;
            this.next = next;

            ranks.add(this);
        }

        public Rank(String tag, String name, String displayName) {
            this(tag, name, displayName, null, null);
        }
    }

    public static class Requirements {
        public final int playTime;
        public final int buildingsBuilt;
        public final int gamesPlayed;

        public Requirements(int playTime, int buildingsBuilt, int gamesPlayed) {
            this.playTime = playTime;
            this.buildingsBuilt = buildingsBuilt;
            this.gamesPlayed = gamesPlayed;
        }

        public boolean check(int playTime, int buildingsBuilt, int gamesPlayed) {
            return playTime >= this.playTime && buildingsBuilt >= this.buildingsBuilt && gamesPlayed >= this.gamesPlayed;
        }
    }
}
