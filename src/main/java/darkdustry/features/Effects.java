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

    public static FxPack pack1, pack2, pack3, pack4, pack5, pack6, pack7;

    public static void load() {
        pack1 = new FxPack(p -> on(Fx.greenBomb, p),                                          p -> on(Fx.greenLaserCharge, p),                                      p -> on(Fx.freezing, p));
        pack2 = new FxPack(p -> on(Fx.dynamicSpikes, p, Mathf.random(40f, 100f), Color.lime), p -> on(Fx.dynamicSpikes, p, Mathf.random(40f, 100f), Color.scarlet), p -> on(Fx.burning, p));
        pack3 = new FxPack(p -> on(Fx.titanExplosion, p),                                     p -> on(Fx.titanExplosion, p),                                        p -> on(Fx.melting, p));
        pack4 = new FxPack(p -> on(Fx.scatheExplosion, p),                                    p -> on(Fx.scatheExplosion, p),                                       p -> on(Fx.electrified, p));
        pack5 = new FxPack(p -> on(Fx.instBomb, p),                                           p -> on(Fx.instBomb, p),                                              p -> on(Fx.shootPayloadDriver, p, p.unit().rotation - 180f, Color.white));
        pack6 = new FxPack(p -> on(Fx.teleportActivate, p),                                   p -> on(Fx.teleport, p),                                              p -> on(Fx.smeltsmoke, p, 0f, Color.red));
        pack7 = new FxPack(p -> on(Fx.teleportActivate, p),                                   p -> on(Fx.teleport, p),                                              p -> on(Fx.chainLightning, p, 0f, Tmp.c1.randHue(), p.unit()));
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
