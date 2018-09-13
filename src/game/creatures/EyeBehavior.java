package game.creatures;

import behaviors.PositionBehavior;
import engine.Behavior;
import java.util.function.Supplier;
import util.vectors.Vec3d;

public class EyeBehavior extends Behavior {

    public final PositionBehavior position = require(PositionBehavior.class);

    public Vec3d facing = new Vec3d(1, 0, 0);
    public Supplier<Vec3d> eyePos = () -> position.position;

    public void setEyeHeight(double eyeHeight) {
        eyePos = () -> position.position.add(new Vec3d(0, 0, eyeHeight));
    }
}
