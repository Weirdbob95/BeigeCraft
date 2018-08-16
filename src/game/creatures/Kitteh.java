package game.creatures;

import engine.Behavior;
import graphics.Model;
import opengl.Camera;

public class Kitteh extends Behavior {

    public final Creature creature = require(Creature.class);

    @Override
    public void createInner() {
        creature.model.model = Model.load("shadow.vox");
        creature.jumpChance = 0;
        creature.setHitboxFromModel();
    }

    @Override
    public void update(double dt) {
        creature.goal = Camera.camera3d.position;
    }
}
