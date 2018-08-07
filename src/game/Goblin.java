package game;

import behaviors.AccelerationBehavior;
import behaviors.PhysicsBehavior;
import behaviors.SpaceOccupierBehavior;
import behaviors.VelocityBehavior;
import engine.Behavior;
import graphics.Model;
import opengl.Camera;
import util.vectors.Vec3d;

public class Goblin extends Behavior {

    public final ModelBehavior model = require(ModelBehavior.class);
    public final VelocityBehavior velocity = require(VelocityBehavior.class);
    public final AccelerationBehavior acceleration = require(AccelerationBehavior.class);
    public final PhysicsBehavior physics = require(PhysicsBehavior.class);
    public final SpaceOccupierBehavior spaceOccupier = require(SpaceOccupierBehavior.class);

    @Override
    public void createInner() {
        model.model = Model.load("goblin.vox");
        acceleration.acceleration = new Vec3d(0, 0, -32);
        physics.hitboxSize = new Vec3d(.3, .3, model.model.size().z / 32);
    }

    @Override
    public void update(double dt) {
        Vec3d idealVel = new Vec3d(0, 0, 0);
        if (Camera.camera.position.sub(physics.position.position).setZ(0).length() > 2.5) {
            idealVel = Camera.camera.position.sub(physics.position.position).setZ(0).normalize().mul(4);
            if (physics.onGround && (physics.hitWall || Math.random() < dt)) {
                velocity.velocity = velocity.velocity.setZ(9);
            }
            model.rotation = Math.atan2(idealVel.y, idealVel.x) - Math.PI / 2;
        }
        velocity.velocity = velocity.velocity.lerp(idealVel.setZ(velocity.velocity.z), 1 - Math.pow(.005, dt));
    }
}
