package game.creatures;

import behaviors.PositionBehavior;
import behaviors.VelocityBehavior;
import behaviors.ModelBehavior;
import game.creatures.Creature;
import engine.Behavior;
import util.vectors.Vec3d;
import game.combat.Strike;
import opengl.Camera;


public class Enemy extends Behavior{
        public final Creature creature = require(Creature.class);
        int cooldown=40;

        @Override
        public void createInner() {

        }

        @Override
        public void update(double dt){
            cooldown++;
                creature.goal = Camera.camera3d.position;
                if (creature.position.position.sub(Camera.camera3d.position).length()<3 && cooldown>=40){
                        Strike strike = new Strike();
                        strike.position.position = creature.position.position;
                        strike.model.rotation = creature.model.rotation;
                        strike.create();
                        cooldown=0;

                }

        }

}
