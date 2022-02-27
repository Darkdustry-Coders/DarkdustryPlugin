package pandorum.components;

import arc.struct.Seq;
import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.database.models.PlayerModel;
import pandorum.util.Utils;

import static pandorum.events.handlers.MenuHandler.rankIncreaseMenu;
import static pandorum.util.Search.findLocale;

public class Ranks {

    public static Rank player;
    public static Rank active;
    public static Rank activePlus;
    public static Rank veteran;
    public static Rank contributor;
    public static Rank admin;

    public static void init() {
        player = new Rank("", "player", "[accent]Player");
        active = new Rank("[#ffd37f]<[white]\uE800[#ffd37f]> ", "active", "[sky]Active", new Requirements(300 * 60, 25000, 20));
        activePlus = new Rank("[#ffd37f]<[white]\uE813[#ffd37f]> ", "active+", "[cyan]Active+", new Requirements(750 * 60, 50000, 40));
        veteran = new Rank("[#ffd37f]<[gold]\uE809[#ffd37f]> ", "veteran", "[gold]Veteran", new Requirements(1500 * 60, 100000, 100));
        contributor = new Rank("[#ffd37f]<[lime]\uE80F[#ffd37f]> ", "contributor", "[lime]Contributor");
        admin = new Rank("[#ffd37f]<[scarlet]\uE817[#ffd37f]> ", "admin", "[scarlet]Admin");

        player.setNext(active);
        active.setNext(activePlus);
        activePlus.setNext(veteran);
    }

    public static Rank getRank(int index) {
        return Rank.ranks.get(index);
    }

    public static void setRank(String uuid, Rank rank) {
        PlayerModel.find(uuid, playerModel -> {
            playerModel.rank = rank.id;

            if (rank.req != null) {
                playerModel.playTime = Math.max(playerModel.playTime, rank.req.playTime);
                playerModel.buildingsBuilt = Math.max(playerModel.buildingsBuilt, rank.req.buildingsBuilt);
                playerModel.gamesPlayed = Math.max(playerModel.gamesPlayed, rank.req.gamesPlayed);
            }

            playerModel.save();
        });
    }

    public static void resetRank(String uuid) {
        PlayerModel.find(uuid, playerModel -> {
            Rank rank;
            int id = 0;

            while ((rank = getRank(id)) != null && rank.next != null && rank.next.req != null && rank.next.req.check(playerModel.playTime, playerModel.buildingsBuilt, playerModel.gamesPlayed)) {
                id++;
            }

            playerModel.rank = id;
            playerModel.save();
        });
    }

    public static void updateRank(Player player) {
        PlayerModel.find(player, playerModel -> {
            Rank rank = getRank(playerModel.rank);

            if (rank.next != null && rank.next.req != null && rank.next.req.check(playerModel.playTime, playerModel.buildingsBuilt, playerModel.gamesPlayed)) {
                rank = rank.next;

                Call.menu(player.con,
                        rankIncreaseMenu,
                        Bundle.format("events.rank-increase.menu.header", findLocale(player.locale)),
                        Bundle.format("events.rank-increase.menu.content", findLocale(player.locale), rank.tag, rank.displayName, Utils.secondsToMinutes(playerModel.playTime), playerModel.buildingsBuilt, playerModel.gamesPlayed),
                        new String[][] {{Bundle.format("ui.menus.close", findLocale(player.locale))}}
                );

                playerModel.rank = rank.id;
                playerModel.save();
            }

            player.name(rank.tag + "[#" + player.color + "]" + player.getInfo().lastName);
        });
    }

    public static class Rank {
        public static final Seq<Rank> ranks = new Seq<>(true);

        public final String tag;
        public final String name;
        public final String displayName;
        public final Requirements req;
        public final int id;

        public Rank next = null;

        public Rank(String tag, String name, String displayName, Requirements req) {
            this.tag = tag;
            this.name = name;
            this.displayName = displayName;
            this.req = req;
            this.id = ranks.size;

            ranks.add(this);
        }

        public Rank(String tag, String name, String displayName) {
            this(tag, name, displayName, null);
        }

        public void setNext(Rank next) {
            this.next = next;
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
