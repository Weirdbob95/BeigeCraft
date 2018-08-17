package game.creatures;

import behaviors.AccelerationBehavior;
import behaviors.ModelBehavior;
import behaviors.PhysicsBehavior;
import behaviors.PositionBehavior;
import behaviors.SpaceOccupierBehavior;
import behaviors.VelocityBehavior;
import engine.Behavior;
import java.util.Collection;
import util.vectors.Vec3d;

public class Creature extends Behavior {

    public static final Collection<Creature> ALL = track(Creature.class);

    public final PositionBehavior position = require(PositionBehavior.class);
    public final VelocityBehavior velocity = require(VelocityBehavior.class);
    public final AccelerationBehavior acceleration = require(AccelerationBehavior.class);
    public final PhysicsBehavior physics = require(PhysicsBehavior.class);
    public final SpaceOccupierBehavior spaceOccupier = require(SpaceOccupierBehavior.class);
    public final ModelBehavior model = require(ModelBehavior.class);

    public double currentHealth = 10;
    public double maxHealth = 10;

    public Vec3d goal;
    public double speed = 6;
    public double minDist = 6;
    public double jumpSpeed = 9;
    public double jumpChance = 1;

    @Override
    public void createInner() {
        acceleration.acceleration = new Vec3d(0, 0, -32);
    }

    public void setHitboxFromModel() {
        double width = Math.max(model.model.size().x, model.model.size().y);
        physics.hitboxSize1 = physics.hitboxSize2 = new Vec3d(width, width, model.model.size().z).div(32);
    }

    @Override
    public void update(double dt) {
        Vec3d idealVel = new Vec3d(0, 0, 0);
        if (goal != null) {
            Vec3d delta = goal.sub(physics.position.position).setZ(0);
            if (delta.length() > minDist) {
                idealVel = delta.normalize().mul(speed);
                if (physics.onGround && (physics.hitWall || Math.random() < dt * jumpChance)) {
                    velocity.velocity = velocity.velocity.setZ(9);
                }
                model.rotation = Math.atan2(idealVel.y, idealVel.x);
            }
        }
        velocity.velocity = velocity.velocity.lerp(idealVel.setZ(velocity.velocity.z), 1 - Math.pow(.005, dt));
    }
}
