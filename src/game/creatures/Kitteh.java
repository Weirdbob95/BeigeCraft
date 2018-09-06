package game.creatures;

import engine.Behavior;
import graphics.Model;
import opengl.Camera;

public class Kitteh extends Behavior {

    public final MonsterBehavior monster = require(MonsterBehavior.class);

    @Override
    public void createInner() {
        monster.model.model = Model.load("shadow.vox");
        monster.jumpChance = 0;
        monster.setHitboxFromModel();
    }

    @Override
    public void update(double dt) {
        monster.goal = Camera.camera3d.position;
    }
}
