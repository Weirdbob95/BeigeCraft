package graphics;

import definitions.BlockType;
import static graphics.VoxelRenderer.DIRS;
import java.util.HashSet;
import static util.math.MathUtils.round;
import util.math.Vec3d;

public abstract class Quad {

    static final int OCCLUSION_DIST = 4;
    static final int OCCLUSION_HEIGHT = 1;

    public float x, y, z;
    public int normal;
    public float[] occlusion = {1, 1, 1, 1};

    public void colorAmbientOcclusion(boolean[][][] occluders) {
        double ao00 = getAmbientOcclusion(occluders, 0, 0);
        double ao10 = getAmbientOcclusion(occluders, 1, 0);
        double ao11 = getAmbientOcclusion(occluders, 1, 1);
        double ao01 = getAmbientOcclusion(occluders, 0, 1);
        occlusion = new float[]{(float) ao00, (float) ao10, (float) ao11, (float) ao01};
    }

    public void colorShadow(double shadow) {
        for (int i = 0; i < 4; i++) {
            occlusion[i] *= shadow;
        }
    }

    private static double getAmbientOcclusion(boolean[][][] a, int i, int j) {
        double solidity = 0;
        for (int i2 = 0; i2 < 2 * OCCLUSION_DIST; i2++) {
            for (int j2 = 0; j2 < 2 * OCCLUSION_DIST; j2++) {
                for (int k2 = 0; k2 < OCCLUSION_HEIGHT; k2++) {
//                    Vec3d blockPos = new Vec3d(i2 - OCCLUSION_DIST + .5, j2 - OCCLUSION_DIST + .5, k2 + .5);
//                    Raycast r = new Raycast(null, new Vec3d(i + OCCLUSION_DIST, j + OCCLUSION_DIST, 0), blockPos, blockPos.length());
//                    if (r.stream().anyMatch(rh -> a[(int) rh.hitPos.x][(int) rh.hitPos.y][(int) rh.hitPos.z])) {
//                        solidity += 1 / blockPos.lengthSquared();
//                    }
                    if (a[i + i2][j + j2][k2]) {
                        double distToCenter2 = (OCCLUSION_DIST - .5 - i2) * (OCCLUSION_DIST - .5 - i2) + (OCCLUSION_DIST - .5 - j2) * (OCCLUSION_DIST - .5 - j2) + k2 * k2;
                        solidity += 1 / distToCenter2;
                    }
                }
            }
        }
        return Math.exp(-solidity * .1);
//        int reachableBlocks = getAmbientOcclusionOctant(a, OCCLUSION_DIST + i, OCCLUSION_DIST + j, 0, 1, 1, 1)
//                + getAmbientOcclusionOctant(a, OCCLUSION_DIST + i - 1, OCCLUSION_DIST + j, 0, -1, 1, 1)
//                + getAmbientOcclusionOctant(a, OCCLUSION_DIST + i, OCCLUSION_DIST + j - 1, 0, 1, -1, 1)
//                + getAmbientOcclusionOctant(a, OCCLUSION_DIST + i - 1, OCCLUSION_DIST + j - 1, 0, -1, -1, 1);
//        double unreachablePerc = 1 - reachableBlocks / (4 * Math.pow(OCCLUSION_DIST, 2) * OCCLUSION_HEIGHT);
//        return Math.exp(-.8 * unreachablePerc);
    }

    private static int getAmbientOcclusionOctant(boolean[][][] a, int i, int j, int k, int iDir, int jDir, int kDir) {
        HashSet<Vec3d> reached = new HashSet();
        if (a[i][j][k]) {
            return 0;
        }
        reached.add(new Vec3d(0, 0, 0));
        for (int i2 = 0; i2 < OCCLUSION_DIST; i2++) {
            for (int j2 = 0; j2 < OCCLUSION_DIST; j2++) {
                for (int k2 = 0; k2 < OCCLUSION_HEIGHT; k2++) {
                    if (!a[i + i2 * iDir][j + j2 * jDir][k + k2 * kDir]) {
                        if (reached.contains(new Vec3d(i2 - 1, j2, k2))
                                || reached.contains(new Vec3d(i2, j2 - 1, k2))
                                || reached.contains(new Vec3d(i2, j2, k2 - 1))) {
                            reached.add(new Vec3d(i2, j2, k2));
                        }
                    }
                }
            }
        }
        return reached.size();
    }

    public int getLOD() {
        int x = round(this.x);
        int y = round(this.y);
        int z = round(this.z);
        int i = 0;
        int m = 1;
        switch (normal) {
            case 0:
            case 1:
                while ((y & m) == 0 && (z & m) == 0) {
                    i += 1;
                    m *= 2;
                    if (i >= 3) {
                        return 3;
                    }
                }
                break;
            case 2:
            case 3:
                while ((x & m) == 0 && (z & m) == 0) {
                    i += 1;
                    m *= 2;
                    if (i >= 3) {
                        return 3;
                    }
                }
                break;
            case 4:
            case 5:
                while ((x & m) == 0 && (y & m) == 0) {
                    i += 1;
                    m *= 2;
                    if (i >= 3) {
                        return 3;
                    }
                }
                break;
        }
        return i;
    }

    public void positionDir(int x, int y, int z, Vec3d dir) {
        this.x = x;
        this.y = y;
        this.z = z;
        normal = DIRS.indexOf(dir);
    }

    public abstract float[] toData();

    public static class ColoredQuad extends Quad {

        public float r, g, b;

        @Override
        public float[] toData() {
            return new float[]{
                x, y, z, normal, r, g, b, occlusion[0], occlusion[1], occlusion[2], occlusion[3]
            };
        }
    }

    public static class TexturedQuad extends Quad {

        public int texID;

        public void texCoordFromBlockType(BlockType bt, Vec3d dir) {
            texID = bt.getTexID(dir);
        }

        @Override
        public float[] toData() {
            return new float[]{
                x, y, z, normal, texID, occlusion[0], occlusion[1], occlusion[2], occlusion[3]
            };
        }
    }
}
