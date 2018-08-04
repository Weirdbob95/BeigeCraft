package behaviors;

import engine.Behavior;
import util.vectors.Vec3d;

public class VelocityBehavior extends Behavior {

    public final PositionBehavior position = require(PositionBehavior.class);

    public Vec3d velocity = new Vec3d(0, 0, 0);

    @Override
    public void update(double dt) {
        position.position = position.position.add(velocity.mul(dt));
    }
}
