package game.combat;

import engine.Property.Modifier;
import static game.combat.Status.StackMode.ADD_DURATION;
import static game.combat.Status.StackMode.SEPARATE;
import game.creatures.CreatureBehavior;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public abstract class Status {

    public final CreatureBehavior creature;

    private final List<Modifier> modifiers = new LinkedList();
    private final double maxTimer;
    private double timer;

    public Status(CreatureBehavior creature, double maxTimer) {
        this.creature = creature;
        this.maxTimer = maxTimer;
    }

    protected void addModifiers(Modifier... m) {
        modifiers.addAll(Arrays.asList(m));
    }

    protected abstract void onStart();

    protected abstract void onUpdate(double dt);

    protected abstract StackMode stackMode();

    public void start() {
        if (stackMode() != SEPARATE) {
            for (Status s : creature.statuses) {
                if (s.getClass().equals(getClass())) {
                    if (stackMode() == ADD_DURATION) {
                        s.timer += maxTimer;
                    } else {
                        s.timer = Math.max(s.timer, maxTimer);
                    }
                    return;
                }
            }
        }
        creature.statuses.add(this);
        timer = maxTimer;
        onStart();
    }

    public void update(double dt) {
        timer -= dt;
        if (timer <= 0) {
            creature.statuses.remove(this);
            modifiers.forEach(m -> m.remove());
        } else {
            onUpdate(dt);
        }
    }

    public static enum StackMode {
        ADD_DURATION,
        MAX_DURATION,
        SEPARATE
    }
}
