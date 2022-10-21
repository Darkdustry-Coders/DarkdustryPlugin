package darkdustry.features;

import arc.func.Cons;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.*;

import static darkdustry.PluginVars.cache;
import static darkdustry.features.Ranks.getRank;
import static mindustry.Vars.state;

public class Effects {

    public static FxPack pack1, pack2, pack3, pack4, pack5, pack6, pack7, pack8;

    public static void load() {
        pack1 = new FxPack(p -> on(Fx.greenBomb, p),               p -> on(Fx.greenLaserCharge, p), p -> on(Fx.freezing, p));
        pack2 = new FxPack(p -> spikes(p, Color.lime),             p -> spikes(p, Color.scarlet),   p -> on(Fx.burning, p));
        pack3 = new FxPack(p -> on(Fx.titanExplosion, p),          p -> on(Fx.titanExplosion, p),   p -> on(Fx.melting, p));
        pack4 = new FxPack(p -> on(Fx.scatheExplosion, p),         p -> on(Fx.scatheExplosion, p),  p -> on(Fx.electrified, p));
        pack5 = new FxPack(p -> on(Fx.neoplasmSplat, p),           p -> on(Fx.neoplasmSplat, p),    p -> on(Fx.overclocked, p, 0f, Tmp.c1.randHue()));
        pack6 = new FxPack(p -> on(Fx.instBomb, p),                p -> on(Fx.instHit, p),          p -> on(Fx.smeltsmoke, p, 0f, Color.orange));
        pack7 = new FxPack(p -> on(Fx.teleportActivate, p),        p -> on(Fx.teleport, p),         p -> on(Fx.smeltsmoke, p, 0f, Color.red));
        pack8 = new FxPack(Effects::particles,                     Effects::particles,              p -> on(Fx.regenSuppressSeek, p, 0f, Color.white, p.unit()));
    }

    public static void on(Effect effect, Position pos, float rotation, Color color, Object data) {
        Call.effect(effect, pos.getX(), pos.getY(), rotation, color, data);
    }

    public static void on(Effect effect, Position pos, float rotation, Color color) {
        Call.effect(effect, pos.getX(), pos.getY(), rotation, color);
    }

    public static void on(Effect effect, Position pos, float rotation) {
        Call.effect(effect, pos.getX(), pos.getY(), rotation, Tmp.c1.randHue());
    }

    public static void on(Effect effect, Position pos) {
        Call.effect(effect, pos.getX(), pos.getY(), Mathf.random(360f), Tmp.c1.randHue());
    }

    public static void spikes(Player player, Color color) {
        on(Fx.dynamicSpikes, player, Mathf.random(40f, 100f), color);
    }

    public static void particles(Player player) {
        for (int deg = 0; deg < 180; deg += 5)
            for (int i = 0; i < 3; i++)
                on(Fx.regenSuppressSeek, Tmp.v1.set(player).add(
                        Mathf.cosDeg(deg + 120f * i) * deg,
                        Mathf.sinDeg(deg + 120f * i) * deg
                ), 0f, Color.white, Tmp.v2.set(player));
    }

    public static void onMove(Player player) {
        if (state.rules.fog || !cache.get(player.id).effects) return;

        getRank(cache.get(player.id).rank).effects.move.get(player);
    }

    public static void onJoin(Player player) {
        if (state.rules.fog || !cache.get(player.id).effects) return;

        getRank(cache.get(player.id).rank).effects.join.get(player);
    }

    public static void onLeave(Player player) {
        if (state.rules.fog || !cache.get(player.id).effects) return;

        getRank(cache.get(player.id).rank).effects.leave.get(player);
    }

    public record FxPack(Cons<Player> join, Cons<Player> leave, Cons<Player> move) {}
}