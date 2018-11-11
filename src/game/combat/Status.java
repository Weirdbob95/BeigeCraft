package game.combat;

import engine.Queryable.Modifier;
import static game.combat.Status.StackMode.ADD_DURATION;
import static game.combat.Status.StackMode.SEPARATE;
import game.creatures.CreatureBehavior;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * The Status class represents any status effect on a creature with a duration
 * in seconds.
 *
 * @author Rory
 */
public abstract class Status {

    /**
     * The creature that this status affects.
     */
    public final CreatureBehavior creature;

    private final List<Modifier> modifiers = new LinkedList();
    private final double maxTimer;
    private double timer;

    /**
     * Constructs a new Status that targets the given creature and lasts the
     * given amount of time.
     *
     * @param creature The creature the status should affect
     * @param maxTimer The duration in seconds of the status
     */
    public Status(CreatureBehavior creature, double maxTimer) {
        this.creature = creature;
        this.maxTimer = maxTimer;
    }

    /**
     * Attaches the given modifiers to this status, so that the modifiers will
     * be removed when this status ends.
     *
     * @param m The modifiers to add
     */
    protected void addModifiers(Modifier... m) {
        modifiers.addAll(Arrays.asList(m));
    }

    /**
     * This function is called when the status takes effect.
     */
    protected abstract void onStart();

    /**
     * This function is called every frame while the status is active.
     *
     * @param dt The amount of time in seconds since the last frame
     */
    protected abstract void onUpdate(double dt);

    /**
     * This function specifies how this status interacts with existing statuses
     * of the same type that target the same creature.
     *
     * @return The StackMode that specifies this status's behavior
     */
    protected abstract StackMode stackMode();

    /**
     * This function makes the status start affecting its target.
     */
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

    /**
     * This function updates the status, and removes it once its duration is
     * finished.
     *
     * @param dt The amount of time in seconds since the last frame
     */
    public void update(double dt) {
        timer -= dt;
        if (timer <= 0) {
            creature.statuses.remove(this);
            modifiers.forEach(m -> m.remove());
        } else {
            onUpdate(dt);
        }
    }

    /**
     * This enum defines how a status should interact with existing status of
     * the same type affecting the same creature.
     */
    public static enum StackMode {

        /**
         * When there are two statuses of the same type affecting the same
         * creature, combine them into a single status with a duration of both
         * durations combined.
         */
        ADD_DURATION,
        /**
         * When there are two statuses of the same type affecting the same
         * creature, keep the one with the longer duration and remove the other.
         */
        MAX_DURATION,
        /**
         * When there are two statuses of the same type affecting the same
         * creature, keep both, just as if they were different types.
         */
        SEPARATE
    }
}
