package game.archetypes;

import engine.Behavior;
import engine.Queryable.Modifier;
import game.abilities.Ability;
import game.abilities.AbilityController;
import game.abilities.Stun;
import game.creatures.CreatureBehavior;
import game.items.HeldItemController;

public class KnightBlock extends Ability {

    public final CreatureBehavior creature = user.get(CreatureBehavior.class);
    public final HeldItemController heldItemController = user.get(HeldItemController.class);

    private Modifier ext1Modifier;
    private Modifier speedModifier;
    private Modifier parryModifier;

    public KnightBlock(Behavior user) {
        super(user);
    }

    @Override
    public void finish(boolean interrupted) {
//        heldItemController.clearAnim();
        ext1Modifier.remove();
        speedModifier.remove();
        parryModifier.remove();
        super.finish(interrupted);
    }

    @Override
    public void start() {
//        SplineAnimation anim = heldItemController.newAnim();
//        anim.addKeyframe(.1, heldItemController.eye.facing.mul(heldItemController.heldItemType.ext1 * .9), new Vec3d(0, 0, 0));

        ext1Modifier = heldItemController.ext1.addModifier(s -> () -> s.get() * .5);
        speedModifier = creature.speed.addModifier(x -> x * .5);
        parryModifier = creature.parryQuery.addModifier(pe -> {
            if (timer < .25) {
                pe.attack.attacker.damage(2, pe.attack.attacker.position.position.sub(creature.position.position).setLength(.5));
                pe.isParried = true;
                pe.attack.attacker.get(AbilityController.class).forceAbility(new Stun(pe.attack.attacker, 1));
            }
            pe.damageMultiplier = .1;
            pe.knockbackMultiplier = .1;
            return pe;
        });
        super.start();
    }
}
