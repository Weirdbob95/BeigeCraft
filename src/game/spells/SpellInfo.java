package game.spells;

import game.creatures.Creature;
import util.vectors.Vec3d;
import util.vectors.Vec4d;
import world.World;

/**
 * The SpellInfo class contains all the information necessary to resolve the
 * effects of a spell.
 *
 * The SpellInfo class stores the spell's target, the spell's direction, the
 * spell's power, and the world the spell lives in. The SpellInfo class is+
 * passed between all the parts of a spell, so that each part of the spell can
 * access necessary information about the instance of the spell. The SpellInfo
 * class is immutable.
 *
 * @author rsoiffer
 */
public class SpellInfo {

    /**
     * The creature or block that this spell targets.
     */
    public final SpellTarget target;

    /**
     * The direction this spell is heading. This is not necessarily a unit
     * vector.
     */
    public final Vec3d direction;

    /**
     * The power of the spell, on a scale from 0 to positive infinity. A power
     * multiplier of 1 is normal.
     */
    public final double powerMultiplier;

    /**
     * The world that this spell is cast in.
     */
    public final World world;

    /**
     * Constructs a new SpellInfo with the given parameters.
     *
     * @param target The target of the spell
     * @param direction The direction of the spell
     * @param powerMultiplier The power of the spell
     * @param world The world the spell lives in
     */
    public SpellInfo(SpellTarget target, Vec3d direction, double powerMultiplier, World world) {
        this.target = target;
        this.direction = direction;
        this.powerMultiplier = powerMultiplier;
        this.world = world;
    }

    /**
     * @return The color of the spell's graphics effects
     */
    public Vec4d color() {
        return new Vec4d(1, .2, 0, 1);
    }

    /**
     * Returns a new SpellInfo whose power has been multiplied by the given
     * amount.
     *
     * @param mult The amount by which to multiply the spell's power
     * @return The new SpellInfo
     */
    public SpellInfo multiplyPower(double mult) {
        return new SpellInfo(target, direction, powerMultiplier * mult, world);
    }

    /**
     * @return The position of the spell's target
     */
    public Vec3d position() {
        if (target.targetsCreature) {
            return target.creature.position.position;
        } else {
            return target.terrain;
        }
    }

    /**
     * Returns a new SpellInfo that targets the given creature.
     *
     * @param creature The new target of the spell
     * @return The new SpellInfo
     */
    public SpellInfo setTarget(Creature creature) {
        return new SpellInfo(new SpellTarget(creature), direction, powerMultiplier, world);
    }

    /**
     * Returns a new SpellInfo that targets the given block.
     *
     * @param terrain The new target of the spell
     * @return The new SpellInfo
     */
    public SpellInfo setTarget(Vec3d terrain) {
        return new SpellInfo(new SpellTarget(terrain), direction, powerMultiplier, world);
    }

    /**
     * The SpellTarget class represents a spell's target, which is either a
     * creature or a block.
     *
     * The SpellTarget class is immutable.
     */
    public static class SpellTarget {

        /**
         * Whether the spell targets a creature
         */
        public final boolean targetsCreature;

        /**
         * The creature that the spell targets (or null if the spell targets a
         * block)
         */
        public final Creature creature;

        /**
         * The block that the spell targets (or null if the spell targets a
         * creature)
         */
        public final Vec3d terrain;

        /**
         * Constructs a new SpellTarget that targets the given creature.
         *
         * @param creature The target of the spell
         */
        public SpellTarget(Creature creature) {
            this.targetsCreature = true;
            this.creature = creature;
            this.terrain = null;
        }

        /**
         * Constructs a new SpellTarget that targets the given block.
         *
         * @param terrain The target of the spell
         */
        public SpellTarget(Vec3d terrain) {
            this.targetsCreature = false;
            this.creature = null;
            this.terrain = terrain;
        }
    }
}
