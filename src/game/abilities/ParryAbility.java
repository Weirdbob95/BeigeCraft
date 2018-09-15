package game.abilities;

import game.HeldItemController;
import game.combat.WeaponAttack;
import game.creatures.CreatureBehavior;
import game.creatures.Skeletor;
import java.util.HashSet;
import java.util.Set;
import util.math.SplineAnimation;
import util.math.Vec3d;

public class ParryAbility extends Ability {

    public CreatureBehavior creature;
    public HeldItemController heldItemController;

    public boolean failed;
    public Set<WeaponAttack> attacksToParry = new HashSet();

    @Override
    public Ability attemptTransitionTo(Ability nextAbility) {
        if (nextAbility instanceof ParryAbility || failed) {
            return new Wait(.5);
        }
        return attacksToParry.isEmpty() ? nextAbility : this;
    }

    private WeaponAttack getParryableAttack(Skeletor s) {
        WeaponAttack wa = WeaponAttack.getFromAbility(s.abilityController.currentAbility);
        if (wa == null
                || !wa.isParryable
                || wa.targetsHit.contains(creature)
                || attacksToParry.contains(wa)) {
            return null;
        }
        return wa;
    }

    @Override
    public void onStartUse() {
        creature = abilityController.get(CreatureBehavior.class);
        heldItemController = abilityController.get(HeldItemController.class);

        SplineAnimation anim = heldItemController.newAnim();
        anim.addKeyframe(.1, heldItemController.eye.facing.mul(heldItemController.heldItemType.ext1 * .6), new Vec3d(0, 0, 0));

        Skeletor toParry = null;
        for (Skeletor s : Skeletor.ALL) {
            if (getParryableAttack(s) != null) {
                double distToS = s.monster.position.position.sub(creature.position.position).length();
                if (distToS < 8) {
                    if (toParry == null || distToS < toParry.monster.position.position.sub(creature.position.position).length()) {
                        toParry = s;
                    }
                }
            }
        }
        if (toParry != null) {
            WeaponAttack wa = getParryableAttack(toParry);
            attacksToParry.add(wa);
            wa.wantToParryThis.add(creature);
        } else {
            failed = true;
        }

        //creature.damageTakenMultiplier = 0;
        creature.speedMultiplier = .5;
        super.onStartUse();
    }

    @Override
    public void onContinuousUse(double dt) {
        attacksToParry.removeIf(wa -> wa.hasFinished || wa.haveParriedThis.contains(creature));
    }

    @Override
    public void onEndUse() {
        heldItemController.clearAnim();
        //creature.damageTakenMultiplier = 1;
        creature.speedMultiplier = 1;
    }
}
