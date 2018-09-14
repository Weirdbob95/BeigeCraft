package game.creatures;

import behaviors.AccelerationBehavior;
import behaviors.PhysicsBehavior;
import behaviors.PositionBehavior;
import behaviors.SpaceOccupierBehavior;
import behaviors.VelocityBehavior;
import engine.Behavior;
import java.util.Collection;
import util.math.Vec3d;

public class CreatureBehavior extends Behavior {

    public static final Collection<CreatureBehavior> ALL = track(CreatureBehavior.class);

    public final PositionBehavior position = require(PositionBehavior.class);
    public final VelocityBehavior velocity = require(VelocityBehavior.class);
    public final AccelerationBehavior acceleration = require(AccelerationBehavior.class);
    public final PhysicsBehavior physics = require(PhysicsBehavior.class);
    public final SpaceOccupierBehavior spaceOccupier = require(SpaceOccupierBehavior.class);

    public double currentHealth = 10;
    public double maxHealth = 10;

    public double speed = 6;
    public double jumpSpeed = 15;

    public double damageTakenMultiplier = 1;
    public double speedMultiplier = 1;

    @Override
    public void createInner() {
        acceleration.acceleration = new Vec3d(0, 0, -64);
    }

    public void damage(double damage, Vec3d dir) {
        velocity.velocity = velocity.velocity.add(dir.mul(20 * damageTakenMultiplier));
        currentHealth -= damage * damageTakenMultiplier;
        if (currentHealth <= 0) {
            getRoot().destroy();
        }
    }

    public double getSpeed() {
        return speed * speedMultiplier;
    }
}
