package pandorum.effects;

import mindustry.gen.*;

public class Effects {

    private static final EffectObject moveEffect = new EffectObject(0, 0, 30, "#4169e1", "freezing");
    private static final EffectObject leaveEffect = new EffectObject(0, 0, 30, "#4169e1", "greenLaserCharge");
    private static final EffectObject joinEffect = new EffectObject(0, 0, 30, "#4169e1", "greenBomb");

    public static void onMove(Player p) {
        try { moveEffect.spawn(p.x, p.y); }
        catch (NullPointerException e) {}
    }

    public static void onJoin(Player p) {        
        try { joinEffect.spawn(p.x, p.y); }
        catch (NullPointerException e) {}
    }

    public static void onLeave(Player p) {
        try { leaveEffect.spawn(p.x, p.y); }
        catch (NullPointerException e) {}
    }
}
