package darkdustry.features;

import arc.struct.Seq;
import darkdustry.database.models.PlayerData;
import lombok.NoArgsConstructor;
import mindustry.gen.Player;
import useful.Bundle;

public class Ranks {

    public static final Seq<Rank> ranks = Seq.with(Rank.values());

    public static void name(Player player, PlayerData data) {
        player.name = data.name = data.rank.tag + player.getInfo().lastName;
    }

    @NoArgsConstructor
    public enum Rank {
        player,

        active(player) {{
            tag = "[#00ffff]<[white]\uE800[]>[] ";
            requirements = new Requirements(320, 12500, 25, 50, 0);
        }},

        hyperActive(active) {{
            tag = "[#00ff00]<[white]\uE813[]>[] ";
            requirements = new Requirements(800, 25000, 50, 100, 0);
        }},

        veteran(hyperActive) {{
            tag = "[#ffff00]<[white]\uE809[]>[] ";
            requirements = new Requirements(2000, 50000, 100, 200, 0);
        }},

        master(veteran) {{
            tag = "[#ff8000]<[white]\uE810[]>[] ";
            requirements = new Requirements(5000, 100000, 200, 400, 0);
        }},

        legend(master) {{
            tag = "[#ff0000]<[white]\uE871[]>[] ";
            requirements = new Requirements(10000, 250000, 500, 1000, 0);
        }},

        mythical(legend) {{
            tag = "[#ff4faa]<[white]\uE80B[]>[] ";
            requirements = new Requirements(50000, 750000, 1000, 7500, 50);
        }},

        tournamentWinner {{
            tag = "[#ffef88]<[white]\uF82C[]>[] ";
        }},

        contentMaker {{
            tag = "[#9999ff]<[white]\uE827[]>[] ";
        }},

        developer {{
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

        Rank(Rank previous) {
            previous.next = this;
        }

        public boolean checkNext(int playTime, int blocksPlaced, int gamesPlayed, int wavesSurvived, int ovas) {
            return next != null && next.requirements != null && next.requirements.check(playTime, blocksPlaced, gamesPlayed, wavesSurvived, ovas);
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

    public record Requirements(int playTime, int blocksPlaced, int gamesPlayed, int wavesSurvived, int ovas) {
        public boolean check(int playTime, int blocksPlaced, int gamesPlayed, int wavesSurvived, int ovas) {
            return this.playTime <= playTime &&
                    this.blocksPlaced <= blocksPlaced &&
                    this.gamesPlayed <= gamesPlayed &&
                    this.wavesSurvived <= wavesSurvived &&
                    this.ovas <= ovas;
        }
    }
}
