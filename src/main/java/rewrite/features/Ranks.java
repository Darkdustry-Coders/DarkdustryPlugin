package rewrite.features;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.gen.Groups;
import mindustry.gen.Player;

import java.util.Locale;

import static rewrite.components.Bundle.*;
import static rewrite.components.Database.*;

public class Ranks {

    public static Rank player, active, activePlus, veteran, contributor, developer, admin, console, owner;
    public static ObjectMap<String, Rank> cache = new ObjectMap<>();

    public static void load() {
        player = new Rank() {{
            tag = "";
            name = "player";
            effects = Effects.defaultPack;

            next = active = new Rank() {{
                tag = "[accent]<[white]\uE800[]>[] ";
                name = "active";
                effects = Effects.defaultPack;
                req = new Requirements(300, 25000, 20);

                next = activePlus = new Rank() {{
                    tag = "[accent]<[white]\uE813[]>[] ";
                    name = "active+";
                    effects = Effects.defaultPack;
                    req = new Requirements(750, 50000, 40);

                    next = veteran = new Rank() {{
                        tag = "[accent]<[gold]\uE809[]>[] ";
                        name = "veteran";
                        effects = Effects.defaultPack;
                        req = new Requirements(1500, 100000, 100);
                    }};
                }};
            }};
        }};

        contributor = new Rank() {{
            tag = "[accent]<[yellow]\uE80F[]>[] ";
            name = "contributor";
            effects = Effects.proPack;
        }};

        developer = new Rank() {{
            tag = "[accent]<[#86dca2]\uE816[]>[] ";
            name = "developer";
            effects = Effects.proPack; // тут по любому будут эти эффекты, ибо я хочу их себе
        }};

        admin = new Rank() {{
            tag = "[accent]<[scarlet]\uE817[]>[] ";
            name = "admin";
            effects = Effects.superPack;
        }};

        console = new Rank() {{
            tag = "[accent]<[#8d56b1]\uE85D[]>[] ";
            name = "console";
            effects = Effects.superPack;
        }};

        owner = new Rank() {{
            tag = "[accent]<[#195080]\uE810[]>[] ";
            name = "owner";
            effects = Effects.superPack;
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

        // обновляем ранг визуально, если игрок находится на сервере
        Groups.player.each(player -> player.uuid().equals(uuid), player -> setRank(player, rank));
    }

    public static class Rank {
        public static final Seq<Rank> ranks = new Seq<>();

        public int id;
        public String tag;
        public String name;
        public Effects.EffectsPack effects;

        public Requirements req;
        public Rank next;

        public Rank() {
            this.id = ranks.size;
            ranks.add(this);
        }

        public boolean checkNext(int playTime, int buildingsBuilt, int gamesPlayed) {
            return next != null && next.req != null && next.req.check(playTime, buildingsBuilt, gamesPlayed);
        }

        // TODO добавить бандлы
        public String localisedName(Locale locale) {
            return get("ranks." + name + ".name", locale);
        }

        public String localisedReq(Locale locale) {
            return format("commands.rank.menu.requirements.content", locale, tag, get(name, locale), req.playTime(), req.buildingsBuilt(), req.gamesPlayed());
        }
    }

    public record Requirements(int playTime, int buildingsBuilt, int gamesPlayed) {
        public boolean check(int playTime, int buildingsBuilt, int gamesPlayed) {
            return playTime >= this.playTime && buildingsBuilt >= this.buildingsBuilt && gamesPlayed >= this.gamesPlayed;
        }
    }
}