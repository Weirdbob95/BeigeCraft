package util;

import java.util.List;
import util.vectors.Vec3d;

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

    public static int floor(double x) {
        return (int) Math.floor(x);
    }

    public static double mod(double x, double m) {
        return (x % m + m) % m;
    }

    public static int mod(int x, int m) {
        return (x % m + m) % m;
    }

    public static float[] vecsToArray(List<Vec3d> vecs) {
        float[] r = new float[3 * vecs.size()];
        for (int i = 0; i < vecs.size(); i++) {
            r[3 * i] = (float) vecs.get(i).x;
            r[3 * i + 1] = (float) vecs.get(i).y;
            r[3 * i + 2] = (float) vecs.get(i).z;
        }
        return r;
    }
}
