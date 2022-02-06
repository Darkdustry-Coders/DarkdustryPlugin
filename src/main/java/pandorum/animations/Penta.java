package pandorum.animations;

import arc.graphics.Color;
import arc.math.geom.Vec2;
import darkdustry.mindustry.effects.animations.Animation;
import darkdustry.mindustry.effects.utils.Engine;
import darkdustry.mindustry.effects.utils.Turtle;
import mindustry.content.Fx;

public class Penta implements Animation {
    public int fieldSize = 100;
    private int maxSteps = fieldSize;
    private int step = 0;
    private Turtle turtle = new Turtle(new Vec2(0 * fieldSize, 0.5f * fieldSize), 180 + 36 / 2);

    @Override
    public Animation next(Vec2 position) {
        step += 4;

        if (step > maxSteps) {
            step = 0;
            turtle.rotate(-144);
        }

        turtle.move(4);

        Engine.draw(
            fieldSize,
            position,
            normalize(turtle.getVector(), fieldSize),
            Fx.bubble,
            Color.violet
        );

        return this;
    }

    @Override
    public float getAnimationSpeedMultiplier() {
        return 40;
    }

    public Vec2 normalize(Vec2 vector, int fieldSize) {
        return new Vec2(
            vector.x / fieldSize,
            vector.y / fieldSize
        );
    }
}