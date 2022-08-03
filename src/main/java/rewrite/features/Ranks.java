package rewrite.features;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Player;

import java.util.Locale;

import static rewrite.components.Bundle.*;
import static rewrite.components.Database.*;

public class Ranks {

    public static Rank player, active, activePlus, veteran, contributor, admin, owner, console, developer;
    public static ObjectMap<String, Rank> cache = new ObjectMap<>();

    public static void load() {
        EffectsPack def = new EffectsPack(Fx.greenBomb, Fx.greenLaserCharge, Fx.freezing);
        EffectsPack pro = new EffectsPack(Fx.instBomb, Fx.instHit, Fx.instTrail);

        player = new Rank() {{
            tag = "";
            name = "player";
            effects = def;

            next = active = new Rank() {{
                tag = "[accent]<[white]\uE800[]>[] ";
                name = "active";
                effects = def;
                req = new Requirements(300, 25000, 20);

                next = activePlus = new Rank() {{
                    tag = "[accent]<[white]\uE813[]>[] ";
                    name = "active+";
                    effects = def;
                    req = new Requirements(750, 50000, 40);

                    next = veteran = new Rank() {{
                        tag = "[accent]<[gold]\uE809[]>[] ";
                        name = "veteran";
                        effects = def;
                        req = new Requirements(1500, 100000, 100);
                    }};
                }};
            }};
        }};

        contributor = new Rank() {{
            tag = "[accent]<[yellow]\uE80F[]>[] ";
            name = "contributor";
            effects = pro;
        }};

        admin = new Rank() {{
            tag = "[accent]<[scarlet]\uE817[]>[] ";
            name = "admin";
            effects = pro;
        }};
        
        owner = new Rank() {{
            tag = "[accent]<[#195080]>[] ";
            name = "owner";
            effects = pro;
        }};
        
        console = new Rank() {{
            tag = "[accent]<[#8d56b1]>[] ";
            name = "console";
            effects = pro;
        }};
        
        developer = new Rank() {{
            tag = "[accent]<[#86dca2]>[] ";
            name = "developer";
            effects = pro; // тут полюбому будут эти эффекты, ибо я хочу их себе
        }};
    }

    public static Rank getRank(int id) {
        return Rank.ranks.get(id);
    }

    public static void setRank(Player player, Rank rank) {
        player.name(rank.tag + player.getInfo().lastName);
        cache.put(player.uuid(), rank);
    }

    public static void setRankNet(String uuid, Rank rank) {
        PlayerData data = getPlayerData(uuid);
        if (data == null) return;

        data.rank = rank.id;
        setPlayerData(data);
    }

    public static class Rank {
        public static final Seq<Rank> ranks = new Seq<>();

        public int id;
        public String tag;
        public String name;
        public EffectsPack effects;

        public Requirements req;
        public Rank next;

        public Rank() {
            this.id = ranks.size;
            ranks.add(this);
        }

        public boolean checkNext(int playTime, int buildingsBuilt, int gamesPlayed) {
            return next != null && next.req != null && next.req.check(playTime, buildingsBuilt, gamesPlayed);
        }

        public String toString(Locale locale) {
            return format("commands.rank.menu.requirements.content", locale, tag, get(name, locale), req.playTime(), req.buildingsBuilt(), req.gamesPlayed());
        }
    }

    public record EffectsPack(Effect join, Effect leave, Effect move) {}

    public record Requirements(int playTime, int buildingsBuilt, int gamesPlayed) {

        public boolean check(int playTime, int buildingsBuilt, int gamesPlayed) {
            return playTime >= this.playTime && buildingsBuilt >= this.buildingsBuilt && gamesPlayed >= this.gamesPlayed;
        }
    }
}
