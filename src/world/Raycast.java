package world;

import definitions.BlockType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import util.Mutable;
import static util.math.MathUtils.mod;
import static util.math.MathUtils.vecMap;
import util.math.Vec3d;
import world.Raycast.RaycastHit;

public class Raycast implements Iterable<RaycastHit> {

    public final World world;
    public final Vec3d pos, dir;
    public final double maxDist;

    public Raycast(World world, Vec3d pos, Vec3d dir) {
        this(world, pos, dir, -1);
    }

    public Raycast(World world, Vec3d pos, Vec3d dir, double maxDist) {
        this.world = world;
        this.pos = pos;
        this.dir = dir;
        this.maxDist = maxDist;
    }

    private Iterator<RaycastHit> infiniteIterator() {
        BinaryOperator<Double> timeToEdge = (x, d) -> (d < 0) ? -x / d : (1 - x) / d;
        Mutable<Vec3d> cp = new Mutable(pos);
        return new Iterator<RaycastHit>() {
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

    @Override
    public Iterator<RaycastHit> iterator() {
        if (maxDist == -1) {
            return infiniteIterator();
        }
        Iterator<RaycastHit> infiniteIterator = infiniteIterator();
        Mutable<RaycastHit> prevResult = new Mutable(infiniteIterator.next());
        return new Iterator<RaycastHit>() {
            @Override
            public boolean hasNext() {
                return prevResult.o != null;
            }

            @Override
            public RaycastHit next() {
                RaycastHit t = prevResult.o;
                prevResult.o = infiniteIterator.next();
                if (prevResult.o.hitPos.sub(pos).length() > maxDist) {
                    prevResult.o = null;
                }
                return t;
            }
        };
    }

    public List<RaycastHit> list() {
        if (maxDist == -1) {
            throw new RuntimeException("Cannot list an infinite raycast");
        }
        ArrayList r = new ArrayList();
        forEach(r::add);
        return r;
    }

    public Stream<RaycastHit> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    public class RaycastHit {

        public final Vec3d hitDir;
        public final Vec3d hitPos;

        public RaycastHit(Vec3d hitDir, Vec3d hitPos) {
            this.hitDir = hitDir;
            this.hitPos = hitPos;
        }

        public BlockType getBlock() {
            return world.getBlock(hitPos);
        }

        public TerrainObjectInstance getTerrainObject() {
            return world.getTerrainObject(hitPos);
        }

        public boolean isEmpty() {
            return getBlock() == null && getTerrainObject() == null;
        }
    }
}
