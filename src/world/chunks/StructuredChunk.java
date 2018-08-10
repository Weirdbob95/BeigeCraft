package world.chunks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import static util.MathUtils.ceil;
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
            for (BlockPlan bp : s.blocks) {
                int x = s.x + bp.x - (cc.pos.x - pos.x) * CHUNK_SIZE;
                int y = s.y + bp.y - (cc.pos.y - pos.y) * CHUNK_SIZE;
                if (x >= 0 && x < CHUNK_SIZE && y >= 0 && y < CHUNK_SIZE) {
                    cc.blockStorage.setRange(x, y, s.z + bp.zMin, s.z + bp.zMax, bp.bt);
                }
            }
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
                        (2 + random.nextInt(5) + random.nextInt(5)) * hc.biomemap[x][y].averageTreeHeight()));
            } else if (hc.biomemap[x][y].plurality() == Biome.DESERT && random.nextDouble() < hc.biomemap[x][y].get(Biome.DESERT) * .01) {
                structures.add(new Cactus(x, y, hc.heightmap[x][y] + 1, 2 + random.nextInt(6)));
            }
        }
    }

    public abstract class Structure {

        private final int x, y, z;
        private final List<BlockPlan> blocks = new LinkedList();

        public Structure(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        protected final void add(BlockPlan... bp) {
            blocks.addAll(Arrays.asList(bp));
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
                            add(new BlockPlan(x, y, (int) height + z, BlockType.LEAVES));
                        }
                    }
                }
            }
            add(new BlockPlan(0, 0, (int) height + 1, BlockType.LEAVES));
            add(new BlockPlan(0, 0, 0, (int) height, BlockType.LOG));
            if (random.nextDouble() < (height - 10) / 5.) {
                add(new BlockPlan(1, 0, 0, (int) height, BlockType.LOG));
                add(new BlockPlan(0, 1, 0, (int) height, BlockType.LOG));
                add(new BlockPlan(1, 1, 0, (int) height, BlockType.LOG));
            }
        }
    }

    public class Cactus extends Structure {

        public Cactus(int x, int y, int z, int height) {
            super(x, y, z);
            add(new BlockPlan(0, 0, 0, (int) height, BlockType.CACTUS));
        }
    }

    public static class BlockPlan {

        public final int x, y, zMin, zMax;
        public final BlockType bt;

        public BlockPlan(int x, int y, int z, BlockType bt) {
            this(x, y, z, z, bt);
        }

        public BlockPlan(int x, int y, int zMin, int zMax, BlockType bt) {
            this.x = x;
            this.y = y;
            this.zMin = zMin;
            this.zMax = zMax;
            this.bt = bt;
        }
    }
}
