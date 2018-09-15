package util.math;

public class Vec4d {

    public final double x, y, z, w;

    public Vec4d(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vec4d add(double a) {
        return new Vec4d(x + a, y + a, z + a, w + a);
    }

    public Vec4d add(Vec4d other) {
        return new Vec4d(x + other.x, y + other.y, z + other.z, w + other.w);
    }

    public Vec4d div(double a) {
        return new Vec4d(x / a, y / a, z / a, w / a);
    }

    public Vec4d div(Vec4d other) {
        return new Vec4d(x / other.x, y / other.y, z / other.z, w / other.w);
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
        final Vec4d other = (Vec4d) obj;
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
            return false;
        }
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) {
            return false;
        }
        if (Double.doubleToLongBits(this.z) != Double.doubleToLongBits(other.z)) {
            return false;
        }
        if (Double.doubleToLongBits(this.w) != Double.doubleToLongBits(other.w)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.w) ^ (Double.doubleToLongBits(this.w) >>> 32));
        return hash;
    }

    public Vec4d lerp(Vec4d other, double amt) {
        return mul(1 - amt).add(other.mul(amt));
    }

    public Vec4d mul(double a) {
        return new Vec4d(x * a, y * a, z * a, w * a);
    }

    public Vec4d mul(Vec4d other) {
        return new Vec4d(x * other.x, y * other.y, z * other.z, w * other.w);
    }

    public Vec4d sub(double a) {
        return new Vec4d(x - a, y - a, z - a, w - a);
    }

    public Vec4d sub(Vec4d other) {
        return new Vec4d(x - other.x, y - other.y, z - other.z, w - other.w);
    }

    @Override
    public String toString() {
        return "Vec4d{" + "x=" + x + ", y=" + y + ", z=" + z + ", w=" + w + '}';
    }
}
