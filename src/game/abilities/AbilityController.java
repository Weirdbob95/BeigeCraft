package game.abilities;

import engine.Behavior;
import static game.abilities.Ability.DO_NOTHING;

public class AbilityController extends Behavior {

    public Ability nextAbility = null;
    public Ability currentAbility = DO_NOTHING;

    public void attemptAbility(Ability ability) {
        if (ability == currentAbility) {
            nextAbility = null;
        } else {
            nextAbility = ability;
        }
    }

    @Override
    public void update(double dt) {
        Ability newAbility = currentAbility.attemptTransitionTo(nextAbility);
        if (newAbility != currentAbility) {
            currentAbility.onEndUse();
            currentAbility = newAbility == null ? DO_NOTHING : newAbility;
            nextAbility = null;
            currentAbility.abilityController = this;
            currentAbility.onStartUse();
        }
        currentAbility.onContinuousUse(dt);
    }
}
