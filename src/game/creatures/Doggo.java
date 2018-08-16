package game.creatures;

import engine.Behavior;
import graphics.Model;
import opengl.Camera;

public class Doggo extends Behavior {

    public final Creature creature = require(Creature.class);

    @Override
    public void createInner() {
        creature.model.model = Model.load("bigzig.vox");
        creature.speed = 9;
        creature.setHitboxFromModel();
    }

    @Override
    public void update(double dt) {
        creature.goal = Camera.camera3d.position;
    }
}
