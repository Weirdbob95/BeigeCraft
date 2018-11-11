package game.abilities;

import engine.Behavior;
import engine.Queryable.Modifier;
import game.abilities.Ability.TimedAbility;
import game.creatures.CreatureBehavior;

public class Stun extends TimedAbility {

    private final double duration;
    private Modifier speedModifier;

    public Stun(Behavior user, double duration) {
        super(user);
        this.duration = duration;
    }

    @Override
    public double duration() {
        return duration;
    }

    @Override
    public void finish(boolean interrupted) {
        speedModifier.remove();
        super.finish(interrupted);
    }

    @Override
    public double priority() {
        return 10;
    }

    @Override
    public void start() {
        CreatureBehavior creature = user.get(CreatureBehavior.class);
        speedModifier = creature.speed.addModifier(x -> x * .25);
        super.start();
    }
}
