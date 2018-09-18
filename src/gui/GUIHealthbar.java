package gui;

import game.creatures.CreatureBehavior;
import graphics.Graphics;
import static util.math.MathUtils.clamp;
import util.math.Vec2d;
import util.math.Vec4d;

public class GUIHealthbar extends GUIRectangle {

    private final CreatureBehavior creature;

    public GUIHealthbar(CreatureBehavior creature) {
        this.creature = creature;
        color = new Vec4d(0, 0, 0, .5);
    }

    @Override
    public void render() {
        double healthPerc = clamp(creature.currentHealth.get() / creature.maxHealth.get(), 0, 1);
        Graphics.drawRectangle(getLowerLeft(), 0, size.mul(new Vec2d(healthPerc, 1)), new Vec4d(1 - healthPerc, healthPerc, 0, 1));
        super.render();
    }
}
