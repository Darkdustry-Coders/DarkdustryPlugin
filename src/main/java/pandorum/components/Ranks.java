package pandorum.components;

import arc.func.Cons;
import arc.struct.IntMap;
import arc.util.Strings;
import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.database.databridges.PlayerInfo;

import static pandorum.util.Search.findLocale;
import static pandorum.util.Utils.secondsToMinutes;

public class Ranks {

    public static Rank player;
    public static Rank active;
    public static Rank activePlus;
    public static Rank veteran;
    public static Rank admin;

    public static void init() {
        player = new Rank("[accent]Player", 0, null, active);
        active = new Rank("[accent]<[white]\uE800[accent]> ", "[cyan]Active", 1, new Requirements(250 * 60, 25000, 15), activePlus);
        activePlus = new Rank("[accent]<[white]\uE813[accent]> ", "[sky]Active+", 2, new Requirements(750 * 60, 50000, 30), veteran);
        veteran = new Rank("[accent]<[gold]\uE809[accent]> ", "[gold]Veteran", 3, new Requirements(1500 * 60, 100000, 100), null);
        admin = new Rank("[accent]<[scarlet]\uE817[accent]> ", "[scarlet]Admin", 4);
    }

    public static Rank get(int index) {
        return Rank.ranks.get(index);
    }

    public static Rank getRank(Player player, int index) {
        return player.admin ? admin : get(index);
    }

    public static void updateRank(Player player, Cons<Rank> cons) {
        if (player.admin) {
            cons.get(admin);
            return;
        }

        PlayerInfo.find(player, playerModel -> {
            Rank current = get(playerModel.rank);

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
                PlayerInfo.save(playerModel);
                cons.get(current.next);
                return;
            }

            cons.get(current);
        });
    }

    public static void updateName(Player player, Cons<String> cons) {
        updateRank(player, rank -> cons.get(Strings.format("@[#@]@", rank.tag, player.color.toString(), player.getInfo().lastName)));
    }

    public static void updateName(Player player) {
        updateName(player, player::name);
    }

    public static class Rank {
        public static final IntMap<Rank> ranks = new IntMap<>();

        public final String tag;
        public final String name;
        public final int id;
        public final Rank next;
        public final Requirements req;

        public Rank(String tag, String name, int id, Requirements req, Rank next) {
            this.name = name;
            this.tag = tag;
            this.id = id;
            this.req = req;
            this.next = next;

            ranks.put(id, this);
        }

        public Rank(String name, int id, Requirements req, Rank next) {
            this("", name, id, req, next);
        }

        public Rank(String tag, String name, int id) {
            this(tag, name, id, null, null);
        }
    }

    public static class Requirements {
        public final long playTime;
        public final int buildingsBuilt;
        public final int gamesPlayed;

        public Requirements(long playTime, int buildingsBuilt, int gamesPlayed) {
            this.playTime = playTime;
            this.buildingsBuilt = buildingsBuilt;
            this.gamesPlayed = gamesPlayed;
        }

        public boolean check(long playTime, int buildingsBuilt, int gamesPlayed) {
            return playTime >= this.playTime && buildingsBuilt >= this.buildingsBuilt && gamesPlayed >= this.gamesPlayed;
        }
    }
}
