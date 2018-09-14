package util.math;

import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class MathUtils {

    public static int ceil(double x) {
        return (int) Math.ceil(x);
    }

    public static double clamp(double x, double lower, double upper) {
        return Math.max(lower, Math.min(x, upper));
    }

    public static int clamp(int x, int lower, int upper) {
        return Math.max(lower, Math.min(x, upper));
    }

    public static Vec3d clamp(Vec3d v, Vec3d lower, Vec3d upper) {
        return new Vec3d(clamp(v.x, lower.x, upper.x), clamp(v.y, lower.y, upper.y), clamp(v.z, lower.z, upper.z));
    }

    public static double cubicSpline(double t, double p1, double v1, double p2, double v2) {
        double a = v1 - (p2 - p1);
        double b = -v2 + (p2 - p1);
        return lerp(p1, p2, t) + lerp(a, b, t) * (t * (1 - t));
    }

    public static double direction(Vec2d v) {
        return Math.atan2(v.y, v.x);
    }

    public static double direction1(Vec3d v) {
        return Math.atan2(v.y, v.x);
    }

    public static double direction2(Vec3d v) {
        return -Math.atan2(v.z, Math.sqrt(v.x * v.x + v.y * v.y));
    }

    public static int floor(double x) {
        return (int) Math.floor(x);
    }

    public static double lerp(double x, double y, double amt) {
        return x * (1 - amt) + y * amt;
    }

    public static double mod(double x, double m) {
        return (x % m + m) % m;
    }

    public static int mod(int x, int m) {
        return (x % m + m) % m;
    }

    public static Vec3d moveTowards(Vec3d pos, Vec3d goal, double linearSpeed, double expSpeed, double dt) {
        Vec3d delta = goal.sub(pos);
        double deltaLength = delta.length();
        double moveLength = linearSpeed * dt + deltaLength * (1 - Math.pow(expSpeed, dt));
        return pos.add(delta.mul(Math.min(moveLength / deltaLength, 1)));
    }

    public static Vec3d randomInSphere() {
        while (true) {
            Vec3d v = new Vec3d(Math.random() * 2 - 1, Math.random() * 2 - 1, Math.random() * 2 - 1);
            if (v.lengthSquared() <= 1) {
                return v;
            }
        }
    }

    public static Vec2d rotate(Vec2d v, double angle) {
        return new Vec2d(Math.cos(angle) * v.x - Math.sin(angle) * v.y,
                Math.sin(angle) * v.x + Math.cos(angle) * v.y);
    }

    public static int round(double x) {
        return (int) Math.round(x);
    }

    public static double round(double x, double m) {
        return Math.round(x / m) * m;
    }

    public static Vec3d vecMap(Vec3d v1, Function<Double, Double> f) {
        return new Vec3d(f.apply(v1.x), f.apply(v1.y), f.apply(v1.z));
    }

    public static Vec3d vecMap(Vec3d v1, Vec3d v2, BiFunction<Double, Double, Double> f) {
        return new Vec3d(f.apply(v1.x, v2.x), f.apply(v1.y, v2.y), f.apply(v1.z, v2.z));
    }
}