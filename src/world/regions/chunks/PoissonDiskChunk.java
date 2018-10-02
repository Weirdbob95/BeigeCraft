package world.regions.chunks;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import util.math.MathUtils;
import util.math.Vec2d;
import world.World;
import static world.World.CHUNK_SIZE;
import world.regions.RegionPos;

public class PoissonDiskChunk extends AbstractChunk {

    private static final double POINT_DENSITY = .1;

    private final Set<Point> points = new HashSet();

    public PoissonDiskChunk(World world, RegionPos pos) {
        super(world, pos);
    }

    @Override
    protected void generate() {
        int num = MathUtils.poissonSample(random, POINT_DENSITY * CHUNK_SIZE * CHUNK_SIZE);
        for (int i = 0; i < num; i++) {
            points.add(new Point(random.nextDouble() * CHUNK_SIZE, random.nextDouble() * CHUNK_SIZE));
        }
    }

    public List<Vec2d> getPoints(double minDist) {
        List<Point> allChosen = new LinkedList();
        List<Vec2d> r = new LinkedList();
        TreeSet<Point> allPoints = new TreeSet();
        for (RegionPos rp : pos.nearby(1)) {
            for (Point p : world.getRegionMap(PoissonDiskChunk.class).get(rp).points) {
                allPoints.add(p.translate(new Vec2d(pos.x - rp.x, pos.y - rp.y).mul(-CHUNK_SIZE)));
            }
        }

        for (Point p : allPoints) {
            boolean keepPoint = true;
            for (Point p2 : allChosen) {
                if (p.near(p2, minDist)) {
                    keepPoint = false;
                    break;
                }
            }
            if (keepPoint) {
                allChosen.add(p);
                if (points.contains(p)) {
                    r.add(p.pos());
                }
            }
        }
        return r;
    }

    public class Point implements Comparable<Point> {

        public final double x, y;
        public final double depth;
        public final double radiusMultiplier;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
            this.depth = random.nextDouble();
            double treeDensity = Math.max(.01, world.heightmappedChunks.get(pos).biomemap[(int) x][(int) y].averageTreeDensity());
            this.radiusMultiplier = 1 / Math.pow(treeDensity, .5);
        }

        public Point(double x, double y, double depth, double radiusMultiplier) {
            this.x = x;
            this.y = y;
            this.depth = depth;
            this.radiusMultiplier = radiusMultiplier;
        }

        @Override
        public int compareTo(Point o) {
            return Double.compare(depth, o.depth);
        }

        public boolean near(Point o, double minDist) {
            return pos().sub(o.pos()).length() < (radiusMultiplier + o.radiusMultiplier) / 2 * minDist;
        }

        public Vec2d pos() {
            return new Vec2d(x, y);
        }

        public Point translate(Vec2d v) {
            if (v.x == 0 && v.y == 0) {
                return this;
            }
            return new Point(x + v.x, y + v.y, depth, radiusMultiplier);
        }
    }
}
