package game.abilities;

import engine.Behavior;

public class AbilityController extends Behavior {

    private Ability currentAbility;

    public Ability currentAbility() {
        return currentAbility;
    }

    public void finishAbility() {
        if (currentAbility != null) {
            Ability old = currentAbility;
            currentAbility = null;
            old.finish(false);
        }
    }

    public void tryAbility(Ability a) {
        while (currentAbility != null) {
            if (a.priority() >= currentAbility.priority()) {
                Ability old = currentAbility;
                currentAbility = null;
                old.finish(true);
            } else {
                break;
            }
        }
        if (currentAbility == null) {
            currentAbility = a;
            currentAbility.start();
        }
    }

    public void tryAbilityType(Ability a) {
        if (currentAbility == null || !currentAbility.getClass().equals(a.getClass())) {
            tryAbility(a);
        }
    }

    @Override
    public void update(double dt) {

        if (currentAbility != null) {
            currentAbility.update(dt);
        }
    }
}
