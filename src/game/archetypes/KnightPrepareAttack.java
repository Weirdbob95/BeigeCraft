package game.archetypes;

import engine.Behavior;
import game.abilities.Ability;
import game.creatures.CreatureBehavior;
import game.items.HeldItemController;

public class KnightPrepareAttack extends Ability {

    public final CreatureBehavior creature = user.get(CreatureBehavior.class);
    public final HeldItemController heldItemController = user.get(HeldItemController.class);

    private boolean powerAttack;

    public KnightPrepareAttack(Behavior user) {
        super(user);
    }

    @Override
    public void finish(boolean interrupted) {
        if (!interrupted) {
            if (powerAttack) {
                user.tryAbility(new KnightSlowAttack(user));
            } else {
                user.tryAbility(new KnightFastAttack(user));
            }
        }
        super.finish(interrupted);
    }

    @Override
    public double priority() {
        return -1;
    }

    @Override
    public void update(double dt) {
        if (timer > .5 && !powerAttack) {
            powerAttack = true;
            register(creature.speed.addModifier(x -> x * .8),
                    heldItemController.ext1.addModifier(s -> () -> s.get() * .5));
        }
        super.update(dt);
    }
}
