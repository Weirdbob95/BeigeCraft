package world.structures;

import definitions.BlockType;
import static definitions.Loader.getBlock;
import static util.math.MathUtils.ceil;
import util.math.Vec3d;
import util.noise.NoiseInterpolator;
import world.regions.chunks.StructuredChunk;

public class Tree extends Structure {

    public Tree(StructuredChunk sc, int x1, int y1, int z1, double height) {
        super(sc, x1, y1, z1);
        priority = 5;
        double size = (2 + height) * .7;
        int intSize = ceil(size);
        NoiseInterpolator leaves = new NoiseInterpolator(sc.noise, intSize, intSize, intSize);
        leaves.setTransform(new Vec3d(x1 - intSize, y1 - intSize, z1 - intSize), new Vec3d(2, 2, 2));
        leaves.generate(4, .7 / size);
        BlockType leaf = getLeafType();
        for (int x = -intSize; x <= intSize; x++) {
            for (int y = -intSize; y <= intSize; y++) {
                for (int z = -intSize; z <= intSize; z++) {
                    if (leaves.get(x1 + x, y1 + y, z1 + z) * size > new Vec3d(x, y, z).length()) {
                        blocks.set(x, y, (int) height + z, leaf);
                    }
                }
            }
        }
        blocks.setRange(0, 0, 0, (int) height + 1, leaf);
        blocks.setRange(0, 0, 0, (int) height, getBlock("log"));
        if (sc.random.nextDouble() < (height - 10) / 5.) {
            blocks.setRange(1, 0, 0, (int) height, getBlock("log"));
            blocks.setRange(0, 1, 0, (int) height, getBlock("log"));
            blocks.setRange(1, 1, 0, (int) height, getBlock("log"));
        }
        removeDisconnected(new Vec3d(0, 0, 0));
    }

    public BlockType getLeafType() {
        if (sc.random.nextDouble() < .8) {
            return getBlock("leaves");
        }
        return getBlock(new String[]{"leavesYellow", "leavesOrange", "leavesRed"}[sc.random.nextInt(3)]);
    }
}
