package pandorum.comp;

import arc.graphics.Color;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Call;
import mindustry.gen.Player;

public class Effects {

    private static final Effect moveEffect = Fx.freezing, leaveEffect = Fx.greenLaserCharge, joinEffect = Fx.greenBomb;

    public static void on(Effect effect, float x, float y) {
        Call.effect(effect, x, y, 0, Color.white);
    }

    public static void onMove(Player p) {
        on(moveEffect, p.x, p.y);
    }

    public static void onJoin(Player p) {
        if (p.team().core() != null) on(joinEffect, p.team().core().x, p.team().core().y);
    }

    public static void onLeave(Player p) {
        if (!p.dead()) on(leaveEffect, p.x, p.y);
    }
}
