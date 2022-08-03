package rewrite.features;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Call;
import mindustry.gen.Player;

import static rewrite.features.Ranks.*;

public class Effects {

    public static EffectsPack defaultPack;
    public static EffectsPack proPack;
    public static EffectsPack superPack;

    public static void load() {
        defaultPack = new EffectsPack(Fx.greenBomb, Fx.greenLaserCharge, Fx.freezing);
        proPack = new EffectsPack(Fx.instBomb, Fx.instHit, Fx.instTrail);
        superPack = new EffectsPack(Fx.coreBuildShockwave, Fx.coreBuildShockwave, Fx.bubble);
    }

    public static void on(Effect effect, float x, float y, float rotation, Color color) {
        Call.effect(effect, x, y, rotation, color);
    }

    public static void on(Effect effect, float x, float y) {
        on(effect, x, y, Mathf.random(360f), Tmp.c1.rand());
    }

    public static void onMove(Player player) {
        on(cache.get(player.uuid()).effects.move(), player.x, player.y);
    }

    public static void onJoin(Player player) {
        on(cache.get(player.uuid()).effects.join(), player.x, player.y);
    }

    public static void onLeave(Player player) {
        on(cache.get(player.uuid()).effects.leave(), player.x, player.y);
    }

    public record EffectsPack(Effect join, Effect leave, Effect move) {}
}
