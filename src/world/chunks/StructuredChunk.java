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

public class StructuredChunk extends AbstractChunk {

    private final List<Structure> structures = new ArrayList();

    public StructuredChunk(World world, ChunkPos pos) {
        super(world, pos);
    }

    public void constructIn(ConstructedChunk cc) {
        for (Structure s : structures) {
            for (BlockPlan bp : s.blocks) {
                int x = (int) s.pos.x + bp.x - (cc.pos.x - pos.x) * CHUNK_SIZE;
                int y = (int) s.pos.y + bp.y - (cc.pos.y - pos.y) * CHUNK_SIZE;
                if (x >= 0 && x < CHUNK_SIZE && y >= 0 && y < CHUNK_SIZE) {
                    cc.blockStorage.setRange(x, y, (int) s.pos.z + bp.zMin, (int) s.pos.z + bp.zMax, bp.bt);
                }
            }
        }
    }

    @Override
    protected void generate() {
        HeightmappedChunk hc = world.heightmappedChunks.get(pos);
        for (int i = 0; i < 100; i++) {
            int x = random.nextInt(CHUNK_SIZE);
            int y = random.nextInt(CHUNK_SIZE);
            if (hc.biomemap[x][y].plurality().vegetation > 0 && random.nextDouble() < hc.biomemap[x][y].averageVegetation() * .1) {
                //System.out.println(hc.biomemap[x][y].averageVegetation());
                structures.add(new Tree(new Vec3d(x, y, hc.heightmap[x][y] + 1)));
            }
        }
    }

    public abstract class Structure {

        private final Vec3d pos;
        private final List<BlockPlan> blocks = new LinkedList();

        public Structure(Vec3d pos) {
            this.pos = pos;
        }

        protected final void add(BlockPlan... bp) {
            blocks.addAll(Arrays.asList(bp));
        }
    }

    public class Tree extends Structure {

        public Tree(Vec3d pos) {
            super(pos);

            int height = 2 + random.nextInt(5) + random.nextInt(5);

            double size = (2 + height) * .7;
            int intSize = ceil(size);
            for (int x = -intSize; x <= intSize; x++) {
                for (int y = -intSize; y <= intSize; y++) {
                    for (int z = -intSize; z <= intSize; z++) {
                        if (noise.multi(pos.x + x, pos.y + y, pos.z + z, 4, .7 / size) * size > new Vec3d(x, y, z).length()) {
                            add(new BlockPlan(x, y, height + z, BlockType.LEAVES));
                        }
                    }
                }
            }

//            for (int iter = 0; iter < 10; iter++) {
//                Vec3d v = randomInSphere(random).mul(random.nextDouble() * size).add(new Vec3d(0, 0, height));
//                //for (Vec3d v : raycastDistance(new Vec3d(0, 0, height), randomInSphere(random), random.nextDouble() * size)) {
//                for (int i = -1; i <= 1; i++) {
//                    for (int j = -1; j <= 1; j++) {
//                        for (int k = -1; k <= 1; k++) {
//                            add(new BlockPlan(floor(v.x) + i, floor(v.y) + j, floor(v.z) + k, BlockType.LEAVES));
//                        }
//                    }
//                }
//                //}
//            }
            add(new BlockPlan(0, 0, 0, height, BlockType.LOG));
//            add(new BlockPlan(0, 1, 0, height, BlockType.LOG));
//            add(new BlockPlan(1, 0, 0, height, BlockType.LOG));
//            add(new BlockPlan(1, 1, 0, height, BlockType.LOG));
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
