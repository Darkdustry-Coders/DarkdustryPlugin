package darkdustry.features;

import arc.func.Cons;
import arc.func.Func;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.util.Tmp;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Call;
import mindustry.gen.Player;

import static mindustry.Vars.*;
import static darkdustry.features.Ranks.*;

public class Effects {

    public static FxPack pack1, pack2, pack3, pack4, pack5;

    public static void load() {
        /*
        pack1 = new EffectsPack(Fx.greenBomb,          Fx.greenLaserCharge,   Fx.freezing, player -> Mathf.random(360f));
        pack2 = new EffectsPack(Fx.chainLightning, Fx.none, Fx.burning, player -> 0f);
        pack3 = new EffectsPack(Fx.neoplasmSplat, Fx.neoplasmSplat, Fx.coreBurn, player -> 0f);
        pack4 = new EffectsPack(Fx.instBomb,           Fx.instHit,            Fx.shootPayloadDriver, player -> player.unit().rotation - 180f);
        pack5 = new EffectsPack(Fx.coreBuildShockwave, Fx.coreBuildShockwave, Fx.bubble, player -> 50f);
         */

        //Fx.airBubble.at(null);
        //Fx.teleportOut.at(null);
        //Fx.regenSuppressSeek.at(null);


        pack1 = new FxPack(player -> on(Fx.greenBomb, player),        player -> on(Fx.greenLaserCharge, player),                               player -> on(Fx.freezing, player));
        pack2 = new FxPack(player -> on(Fx.reactorExplosion, player), player -> on(Fx.reactorExplosion, player),                               player -> on(Fx.burning, player));
        pack3 = new FxPack(player -> on(Fx.neoplasmSplat, player),    player -> on(Fx.neoplasmSplat, player),                                  player -> on(Fx.bubble, player, 36f, Tmp.c1.randHue()));
        pack4 = new FxPack(player -> on(Fx.instBomb, player),         player -> on(Fx.instHit, player, Mathf.random(360f), Color.white), player -> on(Fx.shootPayloadDriver, player, player.unit().rotation - 180f, Color.white));
        pack5 = new FxPack(player -> on(Fx.teleportActivate, player), player -> on(Fx.teleport, player),                                       player -> on(Fx.chainLightning, player, 0f, Tmp.c1.randHue(), player));
    }

    public static void on(Effect effect, Position pos, float rotation, Color color, Object data) {
        Call.effect(effect, pos.getX(), pos.getY(), rotation, color, data);
    }

    public static void on(Effect effect, Position pos, float rotation, Color color) {
        Call.effect(effect, pos.getX(), pos.getY(), rotation, color);
    }

    public static void on(Effect effect, Player player) {
        on(effect, player, Mathf.random(360f), Tmp.c1.rand());
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

    public static record EffectsPack(Effect join, Effect leave, Effect move, Func<Player, Float> rotation) {}

    public static record FxPack(Cons<Player> join, Cons<Player> leave, Cons<Player> move) {}
}
