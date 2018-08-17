package util.noise;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import static util.MathUtils.ceil;

public class ZeroCrossing {

    public final boolean positive;
    public final int start, end;

    public ZeroCrossing(boolean positive, int start, int end) {
        this.positive = positive;
        this.start = start;
        this.end = end;
    }

    public static List<ZeroCrossing> findZeroCrossings(Function<Integer, Double> f, int minZ, int maxZ, double maxGrad) {
        List<ZeroCrossing> r = new LinkedList();
        int z = minZ;
        while (z <= maxZ) {
            int startZ = z;
            double val = f.apply(startZ);
            boolean positive = val > 0;
            while (val > 0 == positive) {
                int step = ceil(Math.abs(val) / maxGrad);
                z += step;
                val = f.apply(z);
                if (z > maxZ) {
                    z = maxZ + 1;
                    break;
                }
            }
            ZeroCrossing zc = new ZeroCrossing(positive, startZ, z - 1);
            r.add(zc);
        }
        return r;
    }

    @Override
    public String toString() {
        return "ZeroCrossing{" + "positive=" + positive + ", start=" + start + ", end=" + end + '}';
    }
}
