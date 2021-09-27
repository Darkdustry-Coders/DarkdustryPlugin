package pandorum.effects;

import arc.graphics.Color;
import arc.util.Reflect;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Call;

public class EffectObject {

    public float x;
    public float y;
    public float rotation;
    private final String color;
    private final String effect;

    public EffectObject(float x, float y, float rotation, String color, String effect) {
        this.x = x;
        this.y = y;
        this.rotation = rotation;
        this.color = color;
        this.effect = effect;
    }

    public Color getColor() {
        return Color.valueOf(color);
    }

    public Effect getEffect() {
        return Reflect.get(Fx.class, effect);
    }

    public void spawn(float x, float y) {
        Call.effect(getEffect(), x, y, rotation, getColor());
    }
}
