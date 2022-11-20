package darkdustry.features;

import arc.func.Cons;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.struct.IntMap;
import arc.util.Tmp;
import darkdustry.components.Database.PlayerData;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.*;
import mindustry.graphics.Pal;


public class Effects {

    public static final IntMap<FxData> effectsCache = new IntMap<>();

    public static FxPack pack1, pack2, pack3, pack4, pack5, pack6, pack7, pack8, pack9, pack10;

    public static void load() {
        pack1 = new FxPack(player -> on(Fx.greenBomb, player), player -> on(Fx.greenLaserCharge, player), player -> on(Fx.freezing, player));

        pack2 = new FxPack(player -> spikes(player, Color.lime), player -> spikes(player, Color.scarlet), player -> on(Fx.burning, player));

        pack3 = new FxPack(player -> on(Fx.titanExplosion, player), player -> on(Fx.titanExplosion, player), player -> on(Fx.melting, player));

        pack4 = new FxPack(player -> on(Fx.scatheExplosion, player), player -> on(Fx.scatheExplosion, player), player -> on(Fx.electrified, player));

        pack5 = new FxPack(player -> on(Fx.neoplasmSplat, player), player -> on(Fx.neoplasmSplat, player), player -> on(Fx.overclocked, player, 0f, Tmp.c1.randHue()));

        pack6 = new FxPack(player -> on(Fx.instBomb, player), player -> on(Fx.instHit, player), player -> on(Fx.smeltsmoke, player, 0f, Color.orange));

        pack7 = new FxPack(player -> on(Fx.titanSmoke, player, 0f, Pal.accent), player -> on(Fx.unitSpawn, player, player.unit().rotation, Color.white, player.unit().type), player -> on(Fx.unitAssemble, player, player.unit().rotation - 90, Color.white, player.unit().type));

        pack8 = new FxPack(player -> on(Fx.teleportActivate, player), player -> on(Fx.teleport, player), player -> on(Fx.smeltsmoke, player, 0f, Color.red));

        pack9 = new FxPack(player -> on(Fx.teleportActivate, player), player -> on(Fx.teleport, player), player -> on(Fx.smeltsmoke, player, 0f, Color.purple));

        pack10 = new FxPack(Effects::particles, Effects::particles, player -> on(Fx.regenSuppressSeek, player, 0f, Color.white, player.unit()));
    }

    public static void on(Effect effect, Position pos, float rotation, Color color, Object data) {
        Call.effect(effect, pos.getX(), pos.getY(), rotation, color, data);
    }

    public static void on(Effect effect, Position pos, float rotation, Color color) {
        Call.effect(effect, pos.getX(), pos.getY(), rotation, color);
    }

    public static void on(Effect effect, Position pos) {
        Call.effect(effect, pos.getX(), pos.getY(), Mathf.random(360f), Tmp.c1.randHue());
    }

    public static void spikes(Player player, Color color) {
        on(Fx.dynamicSpikes, player, 80f, color);
    }

    public static void particles(Player player) {
        for (int deg = 0; deg < 180; deg += 5)
            for (int i = 0; i < 3; i++)
                on(Fx.regenSuppressSeek, Tmp.v1.set(player).add(
                        Mathf.cosDeg(deg + 120f * i) * deg,
                        Mathf.sinDeg(deg + 120f * i) * deg
                ), 0f, Color.white, Tmp.v2.set(player));
    }

    public static void updateEffects(Player player, PlayerData data) {
        effectsCache.put(player.id, new FxData(data.rank.effects, data.effects));
    }

    public static void onMove(Player player) {
        if (!effectsCache.containsKey(player.id) || !effectsCache.get(player.id).enabled) return;
        effectsCache.get(player.id).effects.move.get(player);
    }

    public static void onJoin(Player player) {
        if (!effectsCache.containsKey(player.id) || !effectsCache.get(player.id).enabled) return;
        effectsCache.get(player.id).effects.join.get(player);
    }

    public static void onLeave(Player player) {
        if (!effectsCache.containsKey(player.id) || !effectsCache.get(player.id).enabled) return;
        effectsCache.get(player.id).effects.leave.get(player);
    }

    public record FxPack(Cons<Player> join, Cons<Player> leave, Cons<Player> move) {}

    public record FxData(FxPack effects, boolean enabled) {}
}