package pandorum.features;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.entities.Effect;
import mindustry.gen.Call;

import static pandorum.PluginVars.*;

public class Effects {

    public static void on(Effect effect, float x, float y) {
        on(effect, x, y, Mathf.random(360f), Tmp.c1.rand());
    }

    public static void on(Effect effect, float x, float y, float rotation, Color color) {
        Call.effect(effect, x, y, rotation, color);
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
