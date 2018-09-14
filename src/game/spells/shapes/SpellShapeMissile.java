package game.spells.shapes;

import behaviors.LifetimeBehavior;
import behaviors.PositionBehavior;
import behaviors.VelocityBehavior;
import engine.Behavior;
import game.creatures.CreatureBehavior;
import game.spells.SpellInfo;
import game.spells.SpellPart.SpellShapeInitial;
import util.math.MathUtils;

/**
 * The SpellShapeMissile class represents one of a Projectile, Ray, or Lob
 * initial shape.
 *
 * @author rsoiffer
 */
public abstract class SpellShapeMissile extends SpellShapeInitial {

    /**
     * Whether the spell shape should scatter into multiple smaller missiles
     */
    public boolean isMultishot;

    /**
     * Spawns one or more missiles with the given parameters.
     *
     * This function is intended as a helpful function for each of the spell
     * shapes that spawns missiles of some kind. It provides a common
     * implementation of multishot so that each spell shape doesn't have to
     * reimplement it.
     *
     * The behavior to spawn must require MissileBehavior
     *
     * @param info The SpellInfo at which to cast the spell
     * @param c The behavior to spawn
     * @param velocity The speed of the missiles
     */
    public void spawnMissiles(SpellInfo info, Class<? extends Behavior> c, double velocity) {
        try {
            for (int i = 0; i < (isMultishot ? 5 : 1); i++) {
                Behavior b = c.newInstance();
                MissileBehavior mb = b.get(MissileBehavior.class);
                mb.position.position = info.position();
                mb.velocity.velocity = (isMultishot ? info.direction.normalize().add(MathUtils.randomInSphere().mul(.2)) : info.direction)
                        .setLength(velocity);
                mb.spellShape = this;
                mb.info = info.multiplyPower(isMultishot ? .25 : 1);
                b.create();
            }
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * The MissilesBehavior class represents the physical incarnation of a spell
     * missile in the game world.
     */
    public static class MissileBehavior extends Behavior {

        public final PositionBehavior position = require(PositionBehavior.class);
        public final VelocityBehavior velocity = require(VelocityBehavior.class);
        public final LifetimeBehavior lifetime = require(LifetimeBehavior.class);

        public SpellShapeMissile spellShape;
        public SpellInfo info;

        @Override
        public void update(double dt) {
            if (info.world.getBlock(position.position) != null) {
                getRoot().destroy();
            }
            for (CreatureBehavior c : CreatureBehavior.ALL) {
                if (c != info.target.creature) {
                    if (c.physics.containsPoint(position.position)) {
                        spellShape.hit(info.setTarget(c));
                        getRoot().destroy();
                        break;
                    }
                }
            }
        }
    }
}
