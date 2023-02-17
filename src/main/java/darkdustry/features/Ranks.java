package darkdustry.features;

import arc.struct.Seq;
import darkdustry.components.Database.PlayerData;
import mindustry.gen.Player;
import useful.Bundle;

public class Ranks {

    public static final Seq<Rank> ranks = Seq.with(Rank.values());

    public static void updateRank(Player player, PlayerData data) {
        player.name = data.name = data.rank.tag + player.getInfo().lastName;
    }

    public enum Rank {
        player,

        active(player) {{
            tag = "[sky]<[white]\uE800[]>[] ";
            requirements = new Requirements(320, 12500, 25, 50);
        }},

        hyperActive(active) {{
            tag = "[#738adb]<[white]\uE813[]>[] ";
            requirements = new Requirements(800, 25000, 50, 100);
        }},

        veteran(hyperActive) {{
            tag = "[gold]<[white]\uE809[]>[] ";
            requirements = new Requirements(2000, 50000, 100, 200);
        }},

        master(veteran) {{
            tag = "[orange]<[white]\uE810[]>[] ";
            requirements = new Requirements(5000, 100000, 200, 400);
        }},

        contentCreator {{
            tag = "[yellow]<\uE80F>[] ";
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
            return playTime >= this.playTime && blocksPlaced >= this.blocksPlaced && gamesPlayed >= this.gamesPlayed && wavesSurvived >= this.wavesSurvived;
        }
    }
}