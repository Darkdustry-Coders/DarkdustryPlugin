package darkdustry.features;

import arc.struct.Seq;
import darkdustry.components.MongoDB.PlayerData;
import darkdustry.features.Effects.FxPack;
import mindustry.gen.Player;
import useful.Bundle;

import static darkdustry.features.Effects.updateEffects;

public class Ranks {

    public static final Seq<Rank> all = new Seq<>();

    public static Rank player, active, hyperActive, veteran, master, contentCreator, admin, console, owner;

    public static void load() {
        player = new Rank() {{
            tag = "";
            name = "player";
            effects = Effects.pack1;

            next = active = new Rank() {{
                tag = "[sky]<[white]\uE800[]>[] ";
                name = "active";
                effects = Effects.pack2;
                req = new Requirements(300, 25000, 25);

                next = hyperActive = new Rank() {{
                    tag = "[#738adb]<[white]\uE813[]>[] ";
                    name = "hyperActive";
                    effects = Effects.pack3;
                    req = new Requirements(750, 50000, 50);

                    next = veteran = new Rank() {{
                        tag = "[gold]<[white]\uE809[]>[] ";
                        name = "veteran";
                        effects = Effects.pack4;
                        req = new Requirements(1500, 100000, 100);

                        next = master = new Rank() {{
                            tag = "[orange]<[white]\uE810[]>[] ";
                            name = "master";
                            effects = Effects.pack5;
                            req = new Requirements(5000, 250000, 250);
                        }};
                    }};
                }};
            }};
        }};

        contentCreator = new Rank() {{
            tag = "[yellow]<\uE80F>[] ";
            name = "contentCreator";
            effects = Effects.pack6;
        }};

        admin = new Rank() {{
            tag = "[scarlet]<\uE817>[] ";
            name = "admin";
            effects = Effects.pack7;
        }};

        console = new Rank() {{
            tag = "[#8d56b1]<\uE85D>[] ";
            name = "console";
            effects = Effects.pack7;
        }};

        owner = new Rank() {{
            tag = "[#0088ff]<[white]\uF7A9[]>[] ";
            name = "owner";
            effects = Effects.pack8;
        }};
    }

    public static void updateRank(Player player, PlayerData data) {
        player.name(data.rank().tag + player.getInfo().lastName);
        updateEffects(player, data);
    }

    public static class Rank {
        public final int id;
        public String tag;
        public String name;
        public FxPack effects;

        public Requirements req;
        public Rank next;

        public Rank() {
            this.id = all.add(this).size - 1;
        }

        public boolean hasNext() {
            return next != null && next.req != null;
        }

        public boolean checkNext(int playTime, int buildingsBuilt, int gamesPlayed) {
            return hasNext() && next.req.check(playTime, buildingsBuilt, gamesPlayed);
        }

        public String localisedName(Player player) {
            return tag + Bundle.get("ranks." + name + ".name", name, player);
        }

        public String localisedDesc(Player player) {
            return Bundle.get("ranks." + name + ".desc", "", player);
        }

        public String localisedReq(Player player) {
            return Bundle.format("ranks.req", player, localisedName(player), req.playTime(), req.buildingsBuilt(), req.gamesPlayed());
        }
    }

    public record Requirements(int playTime, int buildingsBuilt, int gamesPlayed) {
        public boolean check(int playTime, int buildingsBuilt, int gamesPlayed) {
            return playTime >= this.playTime && buildingsBuilt >= this.buildingsBuilt && gamesPlayed >= this.gamesPlayed;
        }
    }
}