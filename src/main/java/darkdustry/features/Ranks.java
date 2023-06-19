package darkdustry.features;

import arc.struct.Seq;
import darkdustry.components.Database.PlayerData;
import mindustry.gen.Player;
import useful.Bundle;

public class Ranks {

    public static final Seq<Rank> ranks = Seq.with(Rank.values());

    public static void name(Player player, PlayerData data) {
        player.name = data.name = data.rank.tag + player.getInfo().lastName;
    }

    public enum Rank {
        player,

        active(player) {{
            tag = "[#00ffff]<[white]\uE800[]>[] ";
            requirements = new Requirements(320, 12500, 25, 50);
        }},

        hyperActive(active) {{
            tag = "[#00ff00]<[white]\uE813[]>[] ";
            requirements = new Requirements(800, 25000, 50, 100);
        }},

        veteran(hyperActive) {{
            tag = "[#ffff00]<[white]\uE809[]>[] ";
            requirements = new Requirements(2000, 50000, 100, 200);
        }},

        master(veteran) {{
            tag = "[#ff8000]<[white]\uE810[]>[] ";
            requirements = new Requirements(5000, 100000, 200, 400);
        }},

        legend(master) {{
            tag = "[#ff0000]<[white]\uE871[]>[] ";
            requirements = new Requirements(10000, 250000, 500, 1000);
        }},

        contentMaker {{
            tag = "[#86dca2]<[white]\uE80F[]>[] ";
        }},

        sage {{
            tag = "[accent]<[white]\uF6AA[]>[] ";
        }},

        admin {{
            tag = "[scarlet]<\uE817>[] ";
        }},

        console {{
            tag = "[#8d56b1]<\uE85D>[] ";
        }},

        owner {{
            tag = "[#0088ff]<[white]\uF7A9[]>[] ";
        }};

        public String tag = "";

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

        public String name(Player player) {
            return tag + Bundle.get("ranks." + name() + ".name", name(), player);
        }

        public String description(Player player) {
            return Bundle.get("ranks." + name() + ".description", "...", player);
        }

        public String requirements(Player player) {
            return Bundle.format("ranks.requirements", player, name(player), requirements.playTime, requirements.blocksPlaced, requirements.gamesPlayed, requirements.wavesSurvived);
        }
    }

    public record Requirements(int playTime, int blocksPlaced, int gamesPlayed, int wavesSurvived) {
        public boolean check(int playTime, int blocksPlaced, int gamesPlayed, int wavesSurvived) {
            return this.playTime < playTime && this.blocksPlaced < blocksPlaced && this.gamesPlayed < gamesPlayed && this.wavesSurvived < wavesSurvived;
        }
    }
}