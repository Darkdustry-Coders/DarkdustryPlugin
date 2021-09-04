package pandorum.effects;

import mindustry.gen.*;

public class Effects {

    private static final EffectObject moveEffect = new EffectObject(0, 0, 30, "#4169e1", "burning");
    private static final EffectObject leaveEffect = new EffectObject(0, 0, 30, "#4169e1", "greenLaserCharge");
    private static final EffectObject joinEffect = new EffectObject(0, 0, 30, "#4169e1", "greenBomb");

    public static void onMove(Player p) {
        if(!p.dead()) moveEffect.spawn(p.x, p.y);
    }

    public static void onJoin(Player p) {
        if(!p.dead()) joinEffect.spawn(p.x, p.y);
    }

    public static void onLeave(Player p) {
        if(!p.dead()) leaveEffect.spawn(p.x, p.y);
    }
}