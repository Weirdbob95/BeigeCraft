package game.abilities;

import engine.Behavior;
import java.util.LinkedList;
import java.util.Queue;

public class AbilityController extends Behavior {

    private Ability currentAbility;
    private final Queue<Ability> abilityQueue = new LinkedList();

    public Ability currentAbility() {
        return currentAbility;
    }

    public void finishAbility() {
        switchAbility(abilityQueue.poll(), false);
    }

    public void forceAbility(Ability a) {
        switchAbility(a, true);
    }

    public void queueAbility(Ability a) {
        abilityQueue.add(a);
    }

    private void switchAbility(Ability a, boolean interrupted) {
        if (currentAbility != null) {
            currentAbility.finish(interrupted);
        }
        currentAbility = a;
        if (currentAbility != null) {
            currentAbility.start();
        }
    }

    public void tryAbility(Ability a) {
        if (currentAbility == null || a.priority() >= currentAbility.priority()) {
            switchAbility(a, true);
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
