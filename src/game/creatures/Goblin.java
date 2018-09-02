package game.creatures;

import engine.Behavior;
import graphics.Model;
import opengl.Camera;

public class Goblin extends Behavior {

        public final Enemy enemy = require(Enemy.class);

        @Override
        public void createInner() {
                enemy.creature.model.model = Model.load("goblin.vox");
                enemy.creature.speed = 5;
                enemy.creature.setHitboxFromModel();

        }
        @Override
        public void update(double dt){
                enemy.creature.goal = Camera.camera3d.position;

        }

}
