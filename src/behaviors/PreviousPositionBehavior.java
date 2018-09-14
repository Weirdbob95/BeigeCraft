package behaviors;

import engine.Behavior;
import util.math.Vec3d;

public class PreviousPositionBehavior extends Behavior {

    public final PositionBehavior position = require(PositionBehavior.class);

    public Vec3d prevPos = new Vec3d(0, 0, 0);

    @Override
    public void update(double dt) {
        prevPos = position.position;
    }

    @Override
    public double updateLayer() {
        return 10;
    }
}
