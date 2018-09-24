package world.structures;

import definitions.BlockType;
import static definitions.Loader.getBlock;
import definitions.TerrainObjectType;
import static graphics.VoxelRenderer.DIRS;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import static util.math.MathUtils.vecMap;
import util.math.Vec3d;
import util.rlestorage.IntConverter.BlockTypeConverter;
import util.rlestorage.RLEMapStorage;
import world.TerrainObjectInstance;
import world.regions.chunks.StructuredChunk;

public abstract class Structure {

    public final StructuredChunk sc;
    public final int x, y, z;
    public final RLEMapStorage<BlockType> blocks = new RLEMapStorage(new BlockTypeConverter());
    public final List<TerrainObjectInstance> terrainObjects = new LinkedList();

    //public boolean canBeOverwritten = false;
    public double priority;

    private Vec3d maxPos, minPos;

    public Structure(StructuredChunk sc, int x, int y, int z) {
        this.sc = sc;
        this.x = x;
        this.y = y;
        this.z = z;
        this.priority = sc.random.nextDouble();
    }

    private synchronized void computeMaxMin() {
        if (maxPos == null) {
            Set<Vec3d> occupancy = computeOccupancy();
            maxPos = minPos = occupancy.iterator().next();
            for (Vec3d v : occupancy) {
                maxPos = vecMap(maxPos, v, Math::max);
                minPos = vecMap(minPos, v, Math::min);
            }
        }
    }

    private Set<Vec3d> computeOccupancy() {
        Set<Vec3d> r = new HashSet();
        blocks.allColumns().forEach(c -> {
            if (!c.isEmpty()) {
                for (int z = c.minPos() + 1; z <= c.maxPos(); z++) {
                    r.add(new Vec3d(x, y, this.z).add(sc.worldPos(c.x, c.y, z)));
                }
            }
        });
        terrainObjects.forEach(toi -> {
            for (Vec3d v : toi.getOccupancy()) {
                r.add(sc.worldPos().add(v));
            }
        });
        return r;
    }

    public boolean intersects(Structure other) {
        computeMaxMin();
        other.computeMaxMin();
        if (minPos.x > other.maxPos.x || minPos.y > other.maxPos.y || minPos.z > other.maxPos.z
                || maxPos.x < other.minPos.x || maxPos.y < other.minPos.y || maxPos.z < other.minPos.z) {
            return false;
        }
        Set<Vec3d> occupancy = new HashSet(computeOccupancy());
        occupancy.retainAll(other.computeOccupancy());
        return !occupancy.isEmpty();
    }

    public void removeDisconnected(Vec3d start) {
        Set<Vec3d> connectedComponent = new HashSet();
        connectedComponent.add(start);
        Queue<Vec3d> toCheck = new LinkedList();
        toCheck.add(start);
        while (!toCheck.isEmpty()) {
            Vec3d v = toCheck.poll();
            for (Vec3d dir : DIRS) {
                Vec3d v2 = v.add(dir);
                if (!connectedComponent.contains(v2)) {
                    if (blocks.get((int) v2.x, (int) v2.y, (int) v2.z) != null) {
                        connectedComponent.add(v2);
                        toCheck.add(v2);
                    }
                }
            }
        }
        blocks.allColumns().forEach(c -> {
            if (!c.isEmpty()) {
                List<Integer> toRemove = new LinkedList();
                Iterator<Map.Entry<Integer, BlockType>> i = c.iterator();
                Map.Entry<Integer, BlockType> prev = i.next();
                while (i.hasNext()) {
                    Map.Entry<Integer, BlockType> e = i.next();
                    if (e.getValue() != null) {
                        if (!connectedComponent.contains(new Vec3d(c.x, c.y, e.getKey()))) {
                            for (int z = prev.getKey() + 1; z <= e.getKey(); z++) {
                                toRemove.add(z);
                            }
                        }
                    }
                    prev = e;
                }
                for (int z : toRemove) {
                    blocks.set(c.x, c.y, z, null);
                }
            }
        });
    }

    public static class Cactus extends Structure {

        public Cactus(StructuredChunk sc, int x, int y, int z, int height) {
            super(sc, x, y, z);
            blocks.setRange(0, 0, 0, (int) height, getBlock("cactus"));
        }
    }

    public static class SingleTerrainObject extends Structure {

        public SingleTerrainObject(StructuredChunk sc, int x, int y, int z, TerrainObjectType tot) {
            super(sc, x, y, z);
            terrainObjects.add(new TerrainObjectInstance(tot, sc.pos, x, y, z));
            //canBeOverwritten = true;
            priority -= 5;
        }
    }
}
