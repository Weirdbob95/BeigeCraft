package game.abilities;

import engine.Behavior;
import engine.Property.Modifier;
import game.abilities.Ability.Wait;
import game.creatures.CreatureBehavior;

public class Stun extends Wait {

    private Modifier speedModifier;

    public Stun(Behavior user, double timer) {
        super(user, timer);
    }

    @Override
    public void onStartUse() {
        CreatureBehavior creature = user.get(CreatureBehavior.class);
        speedModifier = creature.speed.addModifier(x -> x * .25);
    }

    @Override
    public void onEndUse() {
        speedModifier.remove();
    }
}
