package behaviors;

import engine.Behavior;
import util.math.Vec3d;

public class AccelerationBehavior extends Behavior {

    public final VelocityBehavior velocity = require(VelocityBehavior.class);

    public Vec3d acceleration = new Vec3d(0, 0, 0);

    @Override
    public void update(double dt) {
        velocity.velocity = velocity.velocity.add(acceleration.mul(dt));
        velocity.position.position = velocity.position.position.add(acceleration.mul(dt * dt / 2));
    }
}
