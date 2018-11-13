package game.archetypes;

import engine.Behavior;
import game.abilities.Ability;
import game.abilities.AbilityController;
import game.abilities.Stun;
import game.creatures.CreatureBehavior;
import game.items.HeldItemController;

public class KnightBlock extends Ability {

    public final CreatureBehavior creature = user.get(CreatureBehavior.class);
    public final HeldItemController heldItemController = user.get(HeldItemController.class);

    public KnightBlock(Behavior user) {
        super(user);
    }

    @Override
    public void start() {
        register(creature.speed.addModifier(x -> x * .5),
                creature.parryQuery.addModifier(pe -> {
                    if (timer < .25) {
                        pe.attack.attacker.damage(2, pe.attack.attacker.position.position.sub(creature.position.position).setLength(.5));
                        pe.isParried = true;
                        pe.attack.attacker.get(AbilityController.class).tryAbility(new Stun(pe.attack.attacker, 1));
                    }
                    pe.damageMultiplier = .1;
                    pe.knockbackMultiplier = .1;
                    return pe;
                }),
                heldItemController.ext1.addModifier(s -> () -> s.get() * .5));
        super.start();
    }
}
