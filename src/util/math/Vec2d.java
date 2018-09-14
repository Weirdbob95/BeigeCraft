package util.math;

import org.joml.Vector2d;

public class Vec2d {

    public final double x, y;

    public Vec2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2d add(double a) {
        return new Vec2d(x + a, y + a);
    }

    public Vec2d add(Vec2d other) {
        return new Vec2d(x + other.x, y + other.y);
    }

    public Vec2d div(double a) {
        return new Vec2d(x / a, y / a);
    }

    public Vec2d div(Vec2d other) {
        return new Vec2d(x / other.x, y / other.y);
    }

    public double dot(Vec2d other) {
        return x * other.x + y * other.y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Vec2d other = (Vec2d) obj;
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
            return false;
        }
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) {
            return false;
        }
        return true;
    }

    public Vec2d floor() {
        return new Vec2d(Math.floor(x), Math.floor(y));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 13 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        return hash;
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    public double lengthSquared() {
        return x * x + y * y;
    }

    public Vec2d lerp(Vec2d other, double amt) {
        return mul(1 - amt).add(other.mul(amt));
    }

    public Vec2d mul(double a) {
        return new Vec2d(x * a, y * a);
    }

    public Vec2d mul(Vec2d other) {
        return new Vec2d(x * other.x, y * other.y);
    }

    public Vec2d normalize() {
        return div(length());
    }

    public Vec2d setX(double x) {
        return new Vec2d(x, y);
    }

    public Vec2d setY(double y) {
        return new Vec2d(x, y);
    }

    public Vec2d sub(double a) {
        return new Vec2d(x - a, y - a);
    }

    public Vec2d sub(Vec2d other) {
        return new Vec2d(x - other.x, y - other.y);
    }

    public Vector2d toJOML() {
        return new Vector2d(x, y);
    }

    @Override
    public String toString() {
        return "Vec2d{" + "x=" + x + ", y=" + y + '}';
    }
}
