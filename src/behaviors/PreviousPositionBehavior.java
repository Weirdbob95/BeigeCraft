package behaviors;

import engine.Behavior;
import util.math.Vec3d;

public class PreviousPositionBehavior extends Behavior {

    public final PositionBehavior position = require(PositionBehavior.class);

    public Vec3d prevPos;

    @Override
    public void createInner() {
        prevPos = position.position;
    }

    @Override
    public void update(double dt) {
        prevPos = position.position;
    }

    @Override
    public double updateLayer() {
        return 10;
    }
}
