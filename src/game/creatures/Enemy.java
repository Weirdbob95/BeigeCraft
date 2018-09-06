package game.creatures;

import engine.Behavior;
import game.combat.Strike;
import opengl.Camera;

public class Enemy extends Behavior {

    public final MonsterBehavior monster = require(MonsterBehavior.class);

    public int cooldown = 40;

    @Override
    public void update(double dt) {
        cooldown++;
        monster.goal = Camera.camera3d.position;
        if (monster.position.position.sub(Camera.camera3d.position).length() < 3 && cooldown >= 40) {
            Strike strike = new Strike();
            strike.position.position = monster.position.position;
            strike.model.rotation = monster.model.rotation;
            strike.create();
            cooldown = 0;
        }
    }
}
