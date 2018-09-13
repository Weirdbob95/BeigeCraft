package game.abilities;

import game.HeldItemController;
import game.abilities.Ability.TimedAbility;
import game.creatures.CreatureBehavior;
import util.SplineAnimation;
import util.vectors.Vec3d;

public class ParryAbility extends TimedAbility {

    public CreatureBehavior creature;
    public HeldItemController heldItemController;

    @Override
    public double duration() {
        return .4;
    }

    @Override
    public void onStartUse() {
        creature = abilityController.get(CreatureBehavior.class);
        heldItemController = abilityController.get(HeldItemController.class);

        SplineAnimation anim = heldItemController.newAnim();
        anim.addKeyframe(.2, heldItemController.eye.facing.mul(heldItemController.heldItemType.ext1 * .6), new Vec3d(0, 0, 0));

        creature.damageMultiplier = 0;
        super.onStartUse();
    }

    @Override
    public void onEndUse() {
        heldItemController.clearAnim();
        creature.damageMultiplier = 1;
    }
}
