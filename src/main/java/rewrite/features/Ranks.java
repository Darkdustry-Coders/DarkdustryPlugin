package rewrite.features;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Player;

import static rewrite.components.Database.*;

public class Ranks {
    
    public static Rank player, active, activePlus, veteran, contributor, admin;
    public static ObjectMap<String, Rank> cache = new ObjectMap<>();

    public static void load() {
        EffectsPack def = new EffectsPack(Fx.greenBomb, Fx.greenLaserCharge, Fx.freezing);
        EffectsPack pro = new EffectsPack(Fx.instBomb, Fx.instHit, Fx.instTrail);

        player = new Rank() {{
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
    }

    public record EffectsPack(Effect join, Effect leave, Effect move) {}

    public record Requirements(int playTime, int buildingsBuilt, int gamesPlayed) {

        public boolean check(int playTime, int buildingsBuilt, int gamesPlayed) {
            return playTime >= this.playTime && buildingsBuilt >= this.buildingsBuilt && gamesPlayed >= this.gamesPlayed;
        }
    }
}
