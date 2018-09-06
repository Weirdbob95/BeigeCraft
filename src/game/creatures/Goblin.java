package game.creatures;

import engine.Behavior;
import graphics.Model;
import opengl.Camera;

public class Goblin extends Behavior {

    public final Enemy enemy = require(Enemy.class);

    @Override
    public void createInner() {
        enemy.monster.model.model = Model.load("goblin.vox");
        enemy.monster.creature.speed = 5;
        enemy.monster.setHitboxFromModel();
    }

    @Override
    public void update(double dt) {
        enemy.monster.goal = Camera.camera3d.position;

    }
}
