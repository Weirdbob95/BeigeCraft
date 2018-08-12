package world.chunks;

import static graphics.VoxelRenderer.DIRS;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import static util.MathUtils.ceil;
import util.rlestorage.IntConverter.BlockTypeConverter;
import util.rlestorage.RLEMapStorage;
import util.vectors.Vec3d;
import world.BlockType;
import world.ChunkPos;
import world.World;
import static world.World.CHUNK_SIZE;
import world.biomes.Biome;

public class StructuredChunk extends AbstractChunk {

    private final List<Structure> structures = new ArrayList();

    public StructuredChunk(World world, ChunkPos pos) {
        super(world, pos);
    }

    public void constructIn(ConstructedChunk cc) {
        for (Structure s : structures) {
            s.blocks.copyTo(cc.blockStorage, s.x - (cc.pos.x - pos.x) * CHUNK_SIZE, s.y - (cc.pos.y - pos.y) * CHUNK_SIZE, s.z);
        }
    }

    @Override
    protected void generate() {
        HeightmappedChunk hc = world.heightmappedChunks.get(pos);
        for (int i = 0; i < CHUNK_SIZE * CHUNK_SIZE / 10.; i++) {
            int x = random.nextInt(CHUNK_SIZE);
            int y = random.nextInt(CHUNK_SIZE);
            if (hc.biomemap[x][y].plurality().treeDensity > 0 && random.nextDouble() < hc.biomemap[x][y].averageTreeDensity() * .1) {
                structures.add(new Tree(x, y, hc.heightmap[x][y] + 1,
                        (2 + random.nextInt(8) + random.nextInt(8)) * hc.biomemap[x][y].averageTreeHeight()));
            } else if (hc.biomemap[x][y].plurality() == Biome.COLD_DESERT && random.nextDouble() < hc.biomemap[x][y].get(Biome.COLD_DESERT) * .01) {
                structures.add(new Cactus(x, y, hc.heightmap[x][y] + 1, 2 + random.nextInt(6)));
            }
        }
    }

    public abstract class Structure {

        private final int x, y, z;
        protected final RLEMapStorage<BlockType> blocks = new RLEMapStorage(new BlockTypeConverter());

        public Structure(int x, int y, int z) {
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
                    Iterator<Entry<Integer, BlockType>> i = c.iterator();
                    Entry<Integer, BlockType> prev = i.next();
                    while (i.hasNext()) {
                        Entry<Integer, BlockType> e = i.next();
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
    }

    public class Tree extends Structure {

        public Tree(int x1, int y1, int z1, double height) {
            super(x1, y1, z1);

            double size = (2 + height) * .7;
            int intSize = ceil(size);
            for (int x = -intSize; x <= intSize; x++) {
                for (int y = -intSize; y <= intSize; y++) {
                    for (int z = -intSize; z <= intSize; z++) {
                        if (noise.fbm3d(x1 + x, y1 + y, z1 + z, 4, .7 / size) * size > new Vec3d(x, y, z).length()) {
                            blocks.set(x, y, (int) height + z, BlockType.LEAVES);
                        }
                    }
                }
            }
            blocks.setRange(0, 0, 0, (int) height + 1, BlockType.LEAVES);
            blocks.setRange(0, 0, 0, (int) height, BlockType.LOG);
            if (random.nextDouble() < (height - 10) / 5.) {
                blocks.setRange(1, 0, 0, (int) height, BlockType.LOG);
                blocks.setRange(0, 1, 0, (int) height, BlockType.LOG);
                blocks.setRange(1, 1, 0, (int) height, BlockType.LOG);
            }
            removeDisconnected(new Vec3d(0, 0, 0));
        }
    }

    public class Cactus extends Structure {

        public Cactus(int x, int y, int z, int height) {
            super(x, y, z);
            blocks.setRange(0, 0, 0, (int) height, BlockType.CACTUS);
        }
    }
}
