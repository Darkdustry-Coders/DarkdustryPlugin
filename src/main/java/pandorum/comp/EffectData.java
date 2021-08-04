package pandorum.comp;

import arc.graphics.Color;
import arc.util.Reflect;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Call;

public class EffectData {
    public float x;

    public float y;

    public float rotation;

    public long periodMillis;

    private final String color;

    private final String effect;

    public EffectData(float x, float y, float rotation, long periodMillis, String color, String effect) {
        this.x = x;
        this.y = y;
        this.rotation = rotation;
        this.periodMillis = periodMillis;
        this.color = color;
        this.effect = effect;
    }

    public Color getColor() {
        return Color.valueOf(color);
    }

    public Effect getEffect() {
        return Reflect.get(Fx.class, effect);
    }

    public void spawn() {
        Call.effect(getEffect(), x, y, rotation, getColor());
    }

    public void spawn(float x, float y) {
        Call.effect(getEffect(), x, y, rotation, getColor());
    }
}
