package darkdustry.features;

import darkdustry.components.Database.PlayerData;
import darkdustry.features.Effects.FxPack;
import mindustry.gen.Player;
import useful.Bundle;

import static darkdustry.features.Effects.updateEffects;

public class Ranks {

    public static void updateRank(Player player, PlayerData data) {
        player.name(data.rank.tag + player.getInfo().lastName);
        updateEffects(player, data);
    }

    public enum Rank {
        player {{
            tag = "";
            effects = Effects.pack1;
        }},

        active(player) {{
            tag = "[sky]<[white]\uE800[]>[] ";
            effects = Effects.pack2;

            requirements = new Requirements(320, 12500, 25);
        }},

        hyperActive(active) {{
            tag = "[#738adb]<[white]\uE813[]>[] ";
            effects = Effects.pack3;

            requirements = new Requirements(800, 25000, 50);
        }},

        veteran(hyperActive) {{
            tag = "[gold]<[white]\uE809[]>[] ";
            effects = Effects.pack4;

            requirements = new Requirements(2000, 50000, 100);
        }},

        master(veteran) {{
            tag = "[orange]<[white]\uE810[]>[] ";
            effects = Effects.pack5;

            requirements = new Requirements(5000, 100000, 200);
        }},

        contentCreator {{
            tag = "[yellow]<\uE80F>[] ";
            effects = Effects.pack6;
        }},

        admin {{
            tag = "[scarlet]<\uE817>[] ";
            effects = Effects.pack7;
        }},

        console {{
            tag = "[#8d56b1]<\uE85D>[] ";
            effects = Effects.pack8;
        }},

        owner {{
            tag = "[#0088ff]<[white]\uF7A9[]>[] ";
            effects = Effects.pack9;
        }};

        public String tag;
        public FxPack effects;

        public Rank next;
        public Requirements requirements;

        Rank() {
            this(null);
        }

        Rank(Rank prev) {
            if (prev != null) prev.next = this;
        }

        public boolean hasNext() {
            return next != null && next.requirements != null;
        }

        public boolean checkNext(int playTime, int buildingsBuilt, int gamesPlayed) {
            return hasNext() && next.requirements.check(playTime, buildingsBuilt, gamesPlayed);
        }

        public String localisedName(Player player) {
            return tag + Bundle.get("ranks." + name() + ".name", name(), player);
        }

        public String localisedDesc(Player player) {
            return Bundle.get("ranks." + name() + ".description", "...", player);
        }

        public String localisedReq(Player player) {
            return Bundle.format("ranks.req", player, localisedName(player), requirements.playTime(), requirements.buildingsBuilt(), requirements.gamesPlayed());
        }
    }

    public record Requirements(int playTime, int buildingsBuilt, int gamesPlayed) {
        public boolean check(int playTime, int buildingsBuilt, int gamesPlayed) {
            return playTime >= this.playTime && buildingsBuilt >= this.buildingsBuilt && gamesPlayed >= this.gamesPlayed;
        }
    }
}