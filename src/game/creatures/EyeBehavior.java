package game.creatures;

import behaviors.PositionBehavior;
import engine.Behavior;
import java.util.function.Supplier;
import util.math.MathUtils;
import util.math.Quaternion;
import util.math.Vec3d;

public class EyeBehavior extends Behavior {

    public final PositionBehavior position = require(PositionBehavior.class);

    public Vec3d facing = new Vec3d(1, 0, 0);
    public Supplier<Vec3d> eyePos = () -> position.position;

    public double direction1() {
        return MathUtils.direction1(facing);
    }

    public double direction2() {
        return MathUtils.direction2(facing);
    }

    public void lookAt(Vec3d pos) {
        facing = pos.sub(eyePos.get()).normalize();
    }

    public Quaternion quat() {
        return Quaternion.fromEulerAngles(direction1(), direction2(), 0);
    }

    public Quaternion quatScaled(double d1, double d2) {
        return Quaternion.fromEulerAngles(direction1() * d1, direction2() * d2, 0);
    }

    public void setEyeHeight(double eyeHeight) {
        eyePos = () -> position.position.add(new Vec3d(0, 0, eyeHeight));
    }
}
