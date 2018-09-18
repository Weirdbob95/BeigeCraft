package game.creatures;

import engine.Behavior;
import graphics.Model;
import opengl.Camera;

public class Doggo extends Behavior {

    public final MonsterBehavior monster = require(MonsterBehavior.class);

    @Override
    public void createInner() {
        monster.model.model = Model.load("bigzig.vox");
        monster.creature.speed.setBaseValue(9.);
        monster.setHitboxFromModel();
    }

    @Override
    public void update(double dt) {
        monster.goal = Camera.camera3d.position;
    }
}
