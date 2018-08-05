package world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import static util.MathUtils.mod;
import static util.MathUtils.vecMap;
import util.Mutable;
import util.vectors.Vec3d;

public class Raycast {

    public static Iterable<Vec3d> raycast(Vec3d pos, Vec3d dir) {
        BinaryOperator<Double> timeToEdge = (x, d) -> (d < 0) ? -x / d : (1 - x) / d;
        return () -> {
            Mutable<Vec3d> cp = new Mutable(pos);
            return new Iterator() {
                @Override
                public boolean hasNext() {
                    return true;
                }

                @Override
                public Object next() {
                    Vec3d c = vecMap(cp.o, Math::floor);
                    Vec3d relPos = vecMap(cp.o, d -> mod(d, 1));
                    Vec3d time = vecMap(relPos, dir, timeToEdge);
                    double minTime = .001 + Math.min(time.x, Math.min(time.y, time.z));
                    cp.o = cp.o.add(dir.mul(minTime));
                    return c;
                }
            };
        };
    }

    public static List<Vec3d> raycastDistance(Vec3d pos, Vec3d dir, double maxDist) {
        ArrayList<Vec3d> r = new ArrayList();
        for (Vec3d v : raycast(pos, dir)) {
            if (pos.sub(v).length() < maxDist) {
                r.add(v);
            } else {
                break;
            }
        }
        return r;
    }

    public static Stream<Vec3d> raycastStream(Vec3d pos, Vec3d dir) {
        return StreamSupport.stream(raycast(pos, dir).spliterator(), false);
    }
}
