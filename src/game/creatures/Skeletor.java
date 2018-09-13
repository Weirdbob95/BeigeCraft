package game.creatures;

import engine.Behavior;
import game.HeldItemController;
import static game.abilities.Ability.DO_NOTHING;
import game.abilities.AbilityController;
import game.abilities.WeaponChargeAbility;
import graphics.Model;

public class Skeletor extends Behavior {

    public final MonsterBehavior monster = require(MonsterBehavior.class);
    public final HeldItemController heldItemController = require(HeldItemController.class);
    public final AbilityController abilityController = require(AbilityController.class);

    @Override
    public void createInner() {
        monster.model.model = Model.load("skelesmalllarge.vox");
        monster.jumpChance = 0;
        monster.setHitboxFromModel();
        heldItemController.eye.setEyeHeight(.9);
    }

    @Override
    public void update(double dt) {
        if (monster.goal != null) {
            heldItemController.eye.facing = monster.goal.sub(monster.position.position).normalize();
            if (Math.random() < dt) {
                abilityController.attemptAbility(new WeaponChargeAbility());
            } else if (Math.random() < dt) {
                abilityController.attemptAbility(DO_NOTHING);
            }
        }
    }
}
