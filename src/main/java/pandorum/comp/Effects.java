package pandorum.comp;

import arc.graphics.Color;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Call;
import mindustry.gen.Player;

public class Effects {

    private static EffectObject moveEffect, leaveEffect, joinEffect;

    public static void init() {
        moveEffect = new EffectObject(Color.valueOf("#4169e1"), Fx.freezing);
        leaveEffect = new EffectObject(Color.valueOf("#4169e1"), Fx.greenLaserCharge);
        joinEffect = new EffectObject(Color.valueOf("#4169e1"), Fx.greenBomb);
    }

    public static void on(EffectObject effect, float x, float y) {
        if (effect != null) effect.spawn(x, y);
    }

    public static void onMove(Player p) {
        on(moveEffect, p.x, p.y);
    }

    public static void onJoin(Player p) {
        try {
            on(joinEffect, p.team().core().x, p.team().core().y);
        } catch (NullPointerException ignored) {}
    }

    public static void onLeave(Player p) {
        try {
            on(leaveEffect, p.x, p.y);
        } catch (NullPointerException ignored) {}
    }

    public static class EffectObject {
        public float rotation = 30;
        private final Color color;
        private final Effect effect;

        public void spawn(float x, float y) {
            Call.effect(effect, x, y, rotation, color);
        }

        public EffectObject(Color color, Effect effect) {
            this.color = color;
            this.effect = effect;
        }
    }
}
