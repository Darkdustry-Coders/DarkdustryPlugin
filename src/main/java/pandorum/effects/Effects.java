package pandorum.effects;

import mindustry.gen.Player;

public class Effects {

    private static final EffectObject moveEffect = new EffectObject(0, 0, 30, "#4169e1", "freezing");
    private static final EffectObject leaveEffect = new EffectObject(0, 0, 30, "#4169e1", "greenLaserCharge");
    private static final EffectObject joinEffect = new EffectObject(0, 0, 30, "#4169e1", "greenBomb");

    public static void on(EffectObject effect, float x, float y) {
        effect.spawn(x, y);
    }

    public static void onMove(Player p) {
        on(moveEffect, p.x, p.y);
    }

    public static void onJoin(Player p) {
        try {
            on(joinEffect, p.x, p.y);
        } catch (NullPointerException ignored) {}
    }

    public static void onLeave(Player p) {
        try {
            on(leaveEffect, p.x, p.y);
        } catch (NullPointerException ignored) {}
    }
}