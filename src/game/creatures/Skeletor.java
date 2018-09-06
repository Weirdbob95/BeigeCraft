package game.creatures;

import engine.Behavior;
import graphics.Model;

public class Skeletor extends Behavior {

    public final MonsterBehavior monster = require(MonsterBehavior.class);

    @Override
    public void createInner() {
        monster.model.model = Model.load("skelesmalllarge.vox");
        monster.jumpChance = 0;
        monster.setHitboxFromModel();
    }
}
