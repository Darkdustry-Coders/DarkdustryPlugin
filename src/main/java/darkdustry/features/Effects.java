package darkdustry.features;

import arc.func.Func;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Call;
import mindustry.gen.Player;

import static mindustry.Vars.*;
import static darkdustry.features.Ranks.*;

public class Effects {

    public static EffectsPack defaultPack, proPack, superPack;

    public static void load() {
        defaultPack = new EffectsPack(Fx.greenBomb,          Fx.greenLaserCharge,   Fx.freezing,           player -> Mathf.random(360f));
        proPack =     new EffectsPack(Fx.instBomb,           Fx.instHit,            Fx.shootPayloadDriver, player -> player.unit().rotation - 180f);
        superPack =   new EffectsPack(Fx.coreBuildShockwave, Fx.coreBuildShockwave, Fx.bubble,             player -> 50f);
    }

    public static void on(Effect effect, float x, float y, float rotation, Color color) {
        Call.effect(effect, x, y, rotation, color);
    }

    public static void on(Effect effect, float x, float y) {
        on(effect, x, y, Mathf.random(360f), Tmp.c1.rand());
    }

    public static void onMove(Player player) {
        if (state.rules.fog) return;
        EffectsPack pack = cache.get(player.uuid()).effects;
        on(pack.move, player.x, player.y, pack.rotation.get(player), Tmp.c1.rand());
    }

    public static void onJoin(Player player) {
        on(cache.get(player.uuid()).effects.join, player.x, player.y);
    }

    public static void onLeave(Player player) {
        on(cache.get(player.uuid()).effects.leave, player.x, player.y);
    }

    public static record EffectsPack(Effect join, Effect leave, Effect move, Func<Player, Float> rotation) {}
}
