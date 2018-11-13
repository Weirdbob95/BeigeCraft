package world.structures;

import definitions.BlockType;
import static definitions.Loader.getBlock;
import static util.math.MathUtils.ceil;
import static util.math.MathUtils.floor;
import static util.math.MathUtils.lerp;
import util.math.Vec3d;
import util.noise.NoiseInterpolator;
import world.regions.chunks.StructuredChunk;

public class Tree extends Structure {

    public Tree(StructuredChunk sc, int x1, int y1, int z1, double height) {
        super(sc, x1, y1, z1);
        priority = 5;
        BlockType leaf = getLeafType();
        double craziness = .5;
        Vec3d scale = new Vec3d(1, 1, leaf.gameName.equals("leavesRed") ? 1 : .5);

        if (sc.random.nextDouble() < .0) {
            constructCanopy(0, 0, (int) height, (2 + height) * .75, craziness, leaf);
        } else {
            constructOval(new Vec3d(0, 0, (int) height), scale.mul(2), leaf);
            int numOvals = 15 + sc.random.nextInt(5);
            for (int i = 0; i < numOvals; i++) {
                Vec3d v = new Vec3d(sc.random.nextDouble(), sc.random.nextDouble(), sc.random.nextDouble());
                v = v.mul(10).sub(5).mul(height / 20).mul(scale).add(new Vec3d(0, 0, height));
                double size = (sc.random.nextDouble() * 4 + 3) * height / 20;
                constructOval(v, scale.mul(size), leaf);
            }
        }
//        while (sc.random.nextDouble() < .75) {
//            constructCanopy(sc.random.nextInt(11) - 5, sc.random.nextInt(11) - 5, (int) height + sc.random.nextInt(11) - 5, (2 + height) * 1.5, craziness, leaf);
//        }

        blocks.setRange(0, 0, 0, (int) height, getBlock("log"));
        if (sc.random.nextDouble() < (height - 10) / 5.) {
            blocks.setRange(-1, 0, 0, (int) height, getBlock("log"));
            blocks.setRange(0, -1, 0, (int) height, getBlock("log"));
            blocks.setRange(-1, -1, 0, (int) height, getBlock("log"));
        }
        removeDisconnected(new Vec3d(0, 0, 0));
    }

    public void constructCanopy(int x1, int y1, int z1, double size, double craziness, BlockType leaf) {
        int intSize = ceil(size);
        NoiseInterpolator leaves = new NoiseInterpolator(sc.noise, intSize, intSize, intSize);
        leaves.setTransform(new Vec3d(this.x - intSize, this.y - intSize, this.z - intSize), new Vec3d(2, 2, 2));
        leaves.generate(1, .7 / size);
        for (int x = -intSize; x <= intSize; x++) {
            for (int y = -intSize; y <= intSize; y++) {
                for (int z = -intSize; z <= intSize; z++) {
                    if (lerp(.5, leaves.get(this.x + x, this.y + y, this.z + z), craziness) * size > new Vec3d(x, y, z).length()) {
                        blocks.set(x1 + x, y1 + y, z1 + z, leaf);
                    }
                }
            }
        }
    }

    public void constructOval(Vec3d pos, Vec3d size, BlockType leaf) {
        for (int x = floor(pos.x - size.x); x < pos.x + size.x; x++) {
            for (int y = floor(pos.y - size.y); y < pos.y + size.y; y++) {
                double l = pos.sub(new Vec3d(x, y, pos.z)).div(size).length();
                if (l < 1) {
                    double z = Math.sqrt(1 - l * l) * size.z;
                    blocks.setRange(x, y, floor(pos.z - z), ceil(pos.z + z), leaf);
                }
            }
        }
    }

    public BlockType getLeafType() {
        if (sc.random.nextDouble() < .8) {
            return getBlock("leaves");
        }
        return getBlock(new String[]{"leavesYellow", "leavesOrange", "leavesRed"}[sc.random.nextInt(3)]);
    }
}
