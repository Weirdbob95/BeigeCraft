package game.abilities;

import engine.Behavior;
import engine.Property.Modifier;
import game.combat.WeaponAttack;
import game.creatures.CreatureBehavior;
import game.creatures.Skeletor;
import game.items.HeldItemController;
import java.util.HashSet;
import java.util.Set;
import util.math.SplineAnimation;
import util.math.Vec3d;

public class ParryAbility extends Ability {

    public final CreatureBehavior creature = user.get(CreatureBehavior.class);
    public final HeldItemController heldItemController = user.get(HeldItemController.class);

    public boolean failed;
    public Set<WeaponAttack> attacksToParry = new HashSet();

    private Modifier speedModifier;

    public ParryAbility(Behavior user) {
        super(user);
    }

    @Override
    public Ability attemptTransitionTo(Ability nextAbility) {
        if (nextAbility instanceof ParryAbility || failed) {
            return new Stun(user, .5);
        }
        return this;
        //return attacksToParry.isEmpty() ? nextAbility : this;
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

        speedModifier = creature.speed.addModifier(x -> x * .5);
        super.onStartUse();
    }

    @Override
    public void onContinuousUse(double dt) {
        attacksToParry.removeIf(wa -> wa.hasFinished || wa.haveParriedThis.contains(creature));
    }

    @Override
    public void onEndUse() {
        heldItemController.clearAnim();
        speedModifier.remove();
    }
}
