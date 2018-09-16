package world.structures;

import definitions.BlockType;
import static definitions.Loader.getBlock;
import static definitions.Loader.getTerrainObject;
import static graphics.VoxelRenderer.DIRS;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import util.math.Vec3d;
import util.rlestorage.IntConverter.BlockTypeConverter;
import util.rlestorage.RLEMapStorage;
import world.TerrainObjectInstance;
import world.chunks.StructuredChunk;

public abstract class Structure {

    public final StructuredChunk sc;
    public final int x, y, z;
    public final RLEMapStorage<BlockType> blocks = new RLEMapStorage(new BlockTypeConverter());
    public final List<TerrainObjectInstance> terrainObjects = new LinkedList();

    public Structure(StructuredChunk sc, int x, int y, int z) {
        this.sc = sc;
        this.x = x;
        this.y = y;
        this.z = z;
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

    public static class Fern extends Structure {

        public Fern(StructuredChunk sc, int x, int y, int z) {
            super(sc, x, y, z);
            terrainObjects.add(new TerrainObjectInstance(getTerrainObject("fern"), sc.pos, x, y, z));
        }
    }

    public static class Flower extends Structure {

        public Flower(StructuredChunk sc, int x, int y, int z) {
            super(sc, x, y, z);
            terrainObjects.add(new TerrainObjectInstance(getTerrainObject("flower1"), sc.pos, x, y, z));
        }
    }
}
