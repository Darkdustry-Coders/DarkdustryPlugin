package pandorum.comp;

import arc.graphics.Color;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Call;

public class Effects {

    public static final Effect moveEffect = Fx.freezing, leaveEffect = Fx.greenLaserCharge, joinEffect = Fx.greenBomb;

    public static void on(Effect effect, float x, float y) {
        Call.effect(effect, x, y, 0, Color.white);
    }

    public static void onMove(float x, float y) {
        on(moveEffect, x, y);
    }

    public static void onJoin(float x, float y) {
        on(joinEffect, x, y);
    }

    public static void onLeave(float x, float y) {
        on(leaveEffect, x, y);
    }
}
