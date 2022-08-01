package pandorum.features;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.entities.Effect;
import mindustry.gen.Call;
import mindustry.gen.Player;

import static pandorum.PluginVars.*;

// TODO больше эффектов, кастомные эффекты рангам, и так далее, и тем более
public class Effects {

    public static void on(Effect effect, float x, float y) {
        on(effect, x, y, Mathf.random(360f), Tmp.c1.rand());
    }

    public static void on(Effect effect, float x, float y, float rotation, Color color) {
        Call.effect(effect, x, y, rotation, color);
    }

    public static void onMove(Player player) {
        on(moveEffect, player.x, player.y);
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
