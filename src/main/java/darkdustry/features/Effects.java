package darkdustry.features;

import arc.func.Cons;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Call;
import mindustry.gen.Player;

import static darkdustry.features.Ranks.cache;
import static mindustry.Vars.state;

public class Effects {

    public static FxPack pack1, pack2, pack3, pack4, pack5, pack6;

    public static void load() {
        pack1 = new FxPack(player -> on(Fx.greenBomb, player),        player -> on(Fx.greenLaserCharge, player), player -> on(Fx.freezing, player));
        pack2 = new FxPack(player -> on(Fx.dynamicSpikes, player, Mathf.random(40f, 120f), Color.lime),          player -> on(Fx.dynamicSpikes, player, Mathf.random(40f, 120f), Color.scarlet), player -> on(Fx.burning, player));
        pack3 = new FxPack(player -> on(Fx.scatheExplosion, player),  player -> on(Fx.scatheExplosion, player),  player -> on(Fx.electrified, player));
        pack4 = new FxPack(player -> on(Fx.instBomb, player),         player -> on(Fx.instBomb, player),         player -> on(Fx.shootPayloadDriver, player, player.unit().rotation - 180f, Color.white));
        pack5 = new FxPack(player -> on(Fx.teleportActivate, player), player -> on(Fx.teleport, player),         player -> on(Fx.chainLightning, player, 0f, Tmp.c1.randHue(), player.unit()));
        pack6 = new FxPack(player -> on(Fx.teleportActivate, player), player -> on(Fx.teleport, player),         player -> on(Fx.regenSuppressSeek, player, 0f, Color.white, player.unit()));
    }

    public static void on(Effect effect, Position pos, float rotation, Color color, Object data) {
        Call.effect(effect, pos.getX(), pos.getY(), rotation, color, data);
    }

    public static void on(Effect effect, Position pos, float rotation, Color color) {
        Call.effect(effect, pos.getX(), pos.getY(), rotation, color);
    }

    public static void on(Effect effect, Position pos) {
        on(effect, pos, Mathf.random(360f), Tmp.c1.randHue());
    }

    public static void onMove(Player player) {
        if (state.rules.fog) return;
        cache.get(player.uuid()).effects.move.get(player);
    }

    public static void onJoin(Player player) {
        cache.get(player.uuid()).effects.join.get(player);
    }

    public static void onLeave(Player player) {
        cache.get(player.uuid()).effects.leave.get(player);
    }

    public static record FxPack(Cons<Player> join, Cons<Player> leave, Cons<Player> move) {}
}
