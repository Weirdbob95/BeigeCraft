package game.spells.shapes;

import behaviors.LifetimeBehavior;
import behaviors.PositionBehavior;
import behaviors.VelocityBehavior;
import engine.Behavior;
import game.creatures.Creature;
import game.spells.SpellInfo;
import game.spells.TypeDefinitions.SpellShapeInitial;
import util.MathUtils;
import util.vectors.Vec3d;

public abstract class SpellShapeMissile extends SpellShapeInitial {

    public boolean isMultishot;

    public void spawnMissiles(SpellInfo info, Vec3d goal, Class<? extends Behavior> c, double velocity) {
        try {
            for (int i = 0; i < (isMultishot ? 5 : 1); i++) {
                Behavior b = c.newInstance();
                MissileBehavior mb = b.get(MissileBehavior.class);
                mb.position.position = info.position();
                mb.velocity.velocity = (isMultishot ? info.direction.normalize().add(MathUtils.randomInSphere().mul(.2)) : info.direction)
                        .normalize().mul(velocity);
                mb.spellShape = this;
                mb.info = info.multiplyPower(isMultishot ? .25 : 1);
                b.create();
            }
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static class MissileBehavior extends Behavior {

        public final PositionBehavior position = require(PositionBehavior.class);
        public final VelocityBehavior velocity = require(VelocityBehavior.class);
        public final LifetimeBehavior lifetime = require(LifetimeBehavior.class);

        public SpellShapeMissile spellShape;
        public SpellInfo info;
        public double homingRate;

        @Override
        public void update(double dt) {
            if (info.world.getBlock(position.position) != null) {
                getRoot().destroy();
            }
            for (Creature c : Creature.ALL) {
                if (c.physics.containsPoint(position.position)) {
                    if (c != info.target.creature) {
                        spellShape.hit(info.setTarget(c));
                        getRoot().destroy();
                        break;
                    }
                }
            }
        }
    }
}
