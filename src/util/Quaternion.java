package util;

import util.vectors.Vec3d;

public class Quaternion {

    public static Quaternion IDENTITY = new Quaternion(1, 0, 0, 0);

    public final double a, b, c, d;

    private Quaternion(double a, double b, double c, double d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public double angle() {
        return 2 * Math.atan2(Math.sqrt(b * b + c * c + d * d), a);
    }

    public Vec3d applyTo(Vec3d pos) {
        Quaternion result = mul(new Quaternion(0, pos.x, pos.y, pos.z)).mul(inverse());
        return new Vec3d(result.b, result.c, result.d);
    }

    public Vec3d applyToForwards() {
        Quaternion result = mul(new Quaternion(0, 1, 0, 0)).mul(inverse());
        return new Vec3d(result.b, result.c, result.d);
    }

    public Vec3d axis() {
        if (new Vec3d(b, c, d).lengthSquared() < 1e-12) {
            return new Vec3d(1, 0, 0);
        }
        return new Vec3d(b, c, d).normalize();
    }

    public double direction1() {
        return MathUtils.direction1(applyToForwards());
    }

    public double direction2() {
        return MathUtils.direction2(applyToForwards());
    }

    public Quaternion div(Quaternion other) {
        return other.inverse().mul(this);
    }

    public static Quaternion fromAngleAxis(double angle, Vec3d axis) {
        axis = axis.normalize();
        double sin = Math.sin(angle / 2), cos = Math.cos(angle / 2);
        return new Quaternion(cos, axis.x * sin, axis.y * sin, axis.z * sin);
    }

    public static Quaternion fromEulerAngles(double yaw, double pitch, double roll) {
        double sinYaw = Math.sin(yaw / 2), cosYaw = Math.cos(yaw / 2);
        double sinPitch = Math.sin(pitch / 2), cosPitch = Math.cos(pitch / 2);
        double sinRoll = Math.sin(roll / 2), cosRoll = Math.cos(roll / 2);
        return new Quaternion(
                sinYaw * sinPitch * sinRoll + cosYaw * cosPitch * cosRoll,
                -sinYaw * sinPitch * cosRoll + cosYaw * cosPitch * sinRoll,
                sinYaw * cosPitch * sinRoll + cosYaw * sinPitch * cosRoll,
                sinYaw * cosPitch * cosRoll - cosYaw * sinPitch * sinRoll);
    }

    public Quaternion inverse() {
        return new Quaternion(a, -b, -c, -d);
    }

    public Quaternion lerp(Quaternion other, double amt) {
        return new Quaternion(a * (1 - amt) + other.a * amt,
                b * (1 - amt) + other.b * amt,
                c * (1 - amt) + other.c * amt,
                d * (1 - amt) + other.d * amt).normalize();
    }

    public Quaternion mul(Quaternion other) {
        return new Quaternion(
                a * other.a - b * other.b - c * other.c - d * other.d,
                a * other.b + b * other.a + c * other.d - d * other.c,
                a * other.c - b * other.d + c * other.a + d * other.b,
                a * other.d + b * other.c - c * other.b + d * other.a);
    }

    public Quaternion normalize() {
        double length = Math.sqrt(a * a + b * b + c * c + d * d);
        return new Quaternion(a / length, b / length, c / length, d / length);
    }

    public Quaternion pow(double t) {
        return fromAngleAxis(t * angle(), axis());
    }

    @Override
    public String toString() {
        return "Quaternion{" + "a=" + a + ", b=" + b + ", c=" + c + ", d=" + d + '}';
    }
}
