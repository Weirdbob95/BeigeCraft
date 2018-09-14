package world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import static util.math.MathUtils.mod;
import static util.math.MathUtils.vecMap;
import util.Mutable;
import util.math.Vec3d;

public class Raycast {

    public static Iterable<RaycastHit> raycast(Vec3d pos, Vec3d dir) {
        BinaryOperator<Double> timeToEdge = (x, d) -> (d < 0) ? -x / d : (1 - x) / d;
        Mutable<Vec3d> cp = new Mutable(pos);
        return () -> new Iterator<RaycastHit>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public RaycastHit next() {
                Vec3d relPos = vecMap(cp.o, d -> mod(d, 1));
                Vec3d time = vecMap(relPos, dir, timeToEdge);
                double minTime = Math.min(time.x, Math.min(time.y, time.z));
                Vec3d hitDir = dir.mul(vecMap(time, t -> t == minTime ? t : 0)).normalize();
                cp.o = cp.o.add(dir.mul(minTime + 1e-6));
                return new RaycastHit(hitDir, cp.o);
            }
        };
    }

    public static List<RaycastHit> raycastDistance(Vec3d pos, Vec3d dir, double maxDist) {
        ArrayList<RaycastHit> r = new ArrayList();
        for (RaycastHit v : raycast(pos, dir)) {
            if (pos.sub(v.hitPos).length() < maxDist) {
                r.add(v);
            } else {
                break;
            }
        }
        return r;
    }

    public static Stream<RaycastHit> raycastStream(Vec3d pos, Vec3d dir) {
        return StreamSupport.stream(raycast(pos, dir).spliterator(), false);
    }

    public static class RaycastHit {

        public final Vec3d hitDir;
        public final Vec3d hitPos;

        public RaycastHit(Vec3d hitDir, Vec3d hitPos) {
            this.hitDir = hitDir;
            this.hitPos = hitPos;
        }
    }
}
