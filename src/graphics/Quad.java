package graphics;

import static graphics.VoxelRenderer.DIRS;
import util.vectors.Vec2d;
import util.vectors.Vec3d;
import world.BlockType;

public abstract class Quad {

    public Vec3d[] positions;
    public Vec3d[] colors;

    public void colorAmbientOcclusion(boolean[][] occluders) {
        double ao00 = getAmbientOcclusion(occluders, 0, 0);
        double ao10 = getAmbientOcclusion(occluders, 1, 0);
        double ao11 = getAmbientOcclusion(occluders, 1, 1);
        double ao01 = getAmbientOcclusion(occluders, 0, 1);
        colors = new Vec3d[]{new Vec3d(ao00, ao00, ao00), new Vec3d(ao10, ao10, ao10), new Vec3d(ao11, ao11, ao11), new Vec3d(ao01, ao01, ao01)};
    }

    public void colorWhite() {
        colors = new Vec3d[]{new Vec3d(1, 1, 1), new Vec3d(1, 1, 1), new Vec3d(1, 1, 1), new Vec3d(1, 1, 1)};
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

    public void positionDir(int x, int y, int z, Vec3d dir) {
        int dirID = DIRS.indexOf(dir);
        switch (dirID) {
            case 0:
                positionNormalX(x, y, z);
                break;
            case 1:
                positionNormalX(x + 1, y, z);
                break;
            case 2:
                positionNormalY(x, y, z);
                break;
            case 3:
                positionNormalY(x, y + 1, z);
                break;
            case 4:
                positionNormalZ(x, y, z);
                break;
            case 5:
                positionNormalZ(x, y, z + 1);
                break;
            case 6:
                throw new RuntimeException("Unknown direction " + dir);
        }
    }

    private void positionNormalX(int x, int y, int z) {
        positions = new Vec3d[]{new Vec3d(x, y, z), new Vec3d(x, y + 1, z), new Vec3d(x, y + 1, z + 1), new Vec3d(x, y, z + 1)};
    }

    private void positionNormalY(int x, int y, int z) {
        positions = new Vec3d[]{new Vec3d(x, y, z), new Vec3d(x, y, z + 1), new Vec3d(x + 1, y, z + 1), new Vec3d(x + 1, y, z)};
    }

    private void positionNormalZ(int x, int y, int z) {
        positions = new Vec3d[]{new Vec3d(x, y, z), new Vec3d(x + 1, y, z), new Vec3d(x + 1, y + 1, z), new Vec3d(x, y + 1, z)};
    }

    public abstract float[] toData(int vertex);

    public static class BasicQuad extends Quad {

        @Override
        public float[] toData(int vertex) {
            return new float[]{
                (float) positions[vertex].x, (float) positions[vertex].y, (float) positions[vertex].z,
                (float) colors[vertex].x, (float) colors[vertex].y, (float) colors[vertex].z
            };
        }
    }

    public static class TexturedQuad extends Quad {

        public Vec2d[] texCoords;
        public Vec2d[] texPositions;

        public void texCoordFromBlockType(BlockType bt, Vec3d dir) {
            if (dir.x == 0) {
                texCoords = new Vec2d[]{new Vec2d(1, 1), new Vec2d(1, 0), new Vec2d(0, 0), new Vec2d(0, 1)};
            } else {
                texCoords = new Vec2d[]{new Vec2d(0, 1), new Vec2d(1, 1), new Vec2d(1, 0), new Vec2d(0, 0)};
            }
            Vec2d pos = BlockType.spritesheetPos(bt, dir);
            texPositions = new Vec2d[]{pos, pos, pos, pos};
        }

        @Override
        public float[] toData(int vertex) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
}
