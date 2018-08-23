package graphics;

import static graphics.VoxelRenderer.DIRS;
import static util.MathUtils.round;
import util.vectors.Vec3d;
import world.BlockType;

public abstract class Quad {

    public float x, y, z;
    public int normal;
    public float[] occlusion = {1, 1, 1, 1};

    public void colorAmbientOcclusion(boolean[][] occluders) {
        double ao00 = getAmbientOcclusion(occluders, 0, 0);
        double ao10 = getAmbientOcclusion(occluders, 1, 0);
        double ao11 = getAmbientOcclusion(occluders, 1, 1);
        double ao01 = getAmbientOcclusion(occluders, 0, 1);
        occlusion = new float[]{(float) ao00, (float) ao10, (float) ao11, (float) ao01};
    }

    private static double getAmbientOcclusion(boolean[][] a, int i, int j) {
//        if (a[0][0] || a[1][0] || a[0][1] || a[1][1]) {
//            return .75f;
//        }
        if ((a[i][j] && a[i + 1][j + 1]) || (a[i][j + 1] && a[i + 1][j])) {
            return .55f;
        }
        int numSolid = 0;
        for (int i2 = i; i2 < i + 2; i2++) {
            for (int j2 = j; j2 < j + 2; j2++) {
                if (a[i2][j2]) {
                    numSolid++;
                }
            }
        }
        switch (numSolid) {
            case 2:
                return .7f;
            case 1:
                return .85f;
            default:
                return 1;
        }
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
            texID = BlockType.getTexID(bt, dir);
        }

        @Override
        public float[] toData() {
            return new float[]{
                x, y, z, normal, texID, occlusion[0], occlusion[1], occlusion[2], occlusion[3]
            };
        }
    }
}
