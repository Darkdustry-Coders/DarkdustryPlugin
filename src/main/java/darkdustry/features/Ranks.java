package darkdustry.features;

import arc.struct.Seq;
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
        player,

        active(player) {{
            tag = "[sky]<[white]\uE800[]>[] ";
            effects = Effects.pack2;

            requirements = new Requirements(320, 12500, 25, 50);
        }},

        hyperActive(active) {{
            tag = "[#738adb]<[white]\uE813[]>[] ";
            effects = Effects.pack3;

            requirements = new Requirements(800, 25000, 50, 100);
        }},

        veteran(hyperActive) {{
            tag = "[gold]<[white]\uE809[]>[] ";
            effects = Effects.pack4;

            requirements = new Requirements(2000, 50000, 100, 200);
        }},

        master(veteran) {{
            tag = "[orange]<[white]\uE810[]>[] ";
            effects = Effects.pack5;

            requirements = new Requirements(5000, 100000, 200, 400);
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

        public static final Seq<Rank> all = Seq.with(values());

        public String tag = "";
        public FxPack effects = Effects.pack1;

        public Rank next;
        public Requirements requirements;

        Rank() {}

        Rank(Rank previous) {
            previous.next = this;
        }

        public boolean hasNext() {
            return next != null && next.requirements != null;
        }

        public boolean checkNext(int playTime, int blocksPlaced, int gamesPlayed, int wavesSurvived) {
            return hasNext() && next.requirements.check(playTime, blocksPlaced, gamesPlayed, wavesSurvived);
        }

        public String localisedName(Player player) {
            return tag + Bundle.get("ranks." + name() + ".name", name(), player);
        }

        public String localisedDesc(Player player) {
            return Bundle.get("ranks." + name() + ".description", "...", player);
        }

        public String localisedReq(Player player) {
            return Bundle.format("ranks.req", player, localisedName(player), requirements.playTime, requirements.blocksPlaced, requirements.gamesPlayed, requirements.wavesSurvived);
        }
    }

    public record Requirements(int playTime, int blocksPlaced, int gamesPlayed, int wavesSurvived) {
        public boolean check(int playTime, int blocksPlaced, int gamesPlayed, int wavesSurvived) {
            return playTime >= this.playTime && blocksPlaced >= this.blocksPlaced && gamesPlayed >= this.gamesPlayed && wavesSurvived >= this.wavesSurvived;
        }
    }
}