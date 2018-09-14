package game.abilities;

import game.HeldItemController;
import game.abilities.Ability.TimedAbility;
import game.creatures.CreatureBehavior;
import game.creatures.Skeletor;
import util.math.SplineAnimation;
import util.math.Vec3d;

public class ParryAbility extends TimedAbility {

    public CreatureBehavior creature;
    public HeldItemController heldItemController;

    @Override
    public Ability attemptTransitionTo(Ability nextAbility) {
        if (nextAbility instanceof ParryAbility) {
            return new Wait(.2);
        }
        return super.attemptTransitionTo(nextAbility);
    }

    @Override
    public double duration() {
        return .4;
    }

    @Override
    public void onStartUse() {
        creature = abilityController.get(CreatureBehavior.class);
        heldItemController = abilityController.get(HeldItemController.class);

        SplineAnimation anim = heldItemController.newAnim();
        anim.addKeyframe(.1, heldItemController.eye.facing.mul(heldItemController.heldItemType.ext1 * .6), new Vec3d(0, 0, 0));

        for (Skeletor s : Skeletor.ALL) {
            if (s.monster.position.position.sub(creature.position.position).length() < 5) {
                s.attackParried = true;
            }
        }

        creature.damageTakenMultiplier = 0;
        creature.speedMultiplier = .5;
        super.onStartUse();
    }

    @Override
    public void onEndUse() {
        heldItemController.clearAnim();
        creature.damageTakenMultiplier = 1;
        creature.speedMultiplier = 1;
    }
}
