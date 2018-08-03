package util;

public abstract class MathUtils {

    public static double clamp(double x, double lower, double upper) {
        return Math.max(lower, Math.min(x, upper));
    }

    public static int clamp(int x, int lower, int upper) {
        return Math.max(lower, Math.min(x, upper));
    }

    public static double mod(double x, double m) {
        return (x % m + m) % m;
    }
}
