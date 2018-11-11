package game.creatures;

import behaviors.AccelerationBehavior;
import behaviors.PhysicsBehavior;
import behaviors.PositionBehavior;
import behaviors.SpaceOccupierBehavior;
import behaviors.VelocityBehavior;
import engine.Behavior;
import engine.Property;
import game.combat.Status;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import util.math.Vec3d;

public class CreatureBehavior extends Behavior {

    public static final Collection<CreatureBehavior> ALL = track(CreatureBehavior.class);

    public final PositionBehavior position = require(PositionBehavior.class);
    public final VelocityBehavior velocity = require(VelocityBehavior.class);
    public final AccelerationBehavior acceleration = require(AccelerationBehavior.class);
    public final PhysicsBehavior physics = require(PhysicsBehavior.class);
    public final SpaceOccupierBehavior spaceOccupier = require(SpaceOccupierBehavior.class);

    public Property<Double> currentHealth = new Property(10.);
    public Property<Double> maxHealth = new Property(10.);

    public Property<Double> speed = new Property(6.);
    public Property<Double> jumpSpeed = new Property(10.);
    public Property<Boolean> canMove = new Property(true);

    public Set<Status> statuses = new HashSet();

    @Override
    public void createInner() {
//        acceleration.acceleration = new Vec3d(0, 0, -64);
        acceleration.acceleration = new Vec3d(0, 0, -20);
    }

    public void damage(double damage, Vec3d dir) {
        velocity.velocity = velocity.velocity.add(dir.mul(20));
        currentHealth.setBaseValue(currentHealth.getBaseValue() - damage);
        if (currentHealth.get() <= 0) {
            getRoot().destroy();
        }
    }

    @Override
    public void update(double dt) {
        new LinkedList<>(statuses).forEach(s -> s.update(dt));
    }
}
