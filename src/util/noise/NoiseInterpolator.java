package util.noise;

import static util.math.MathUtils.ceil;
import static util.math.MathUtils.floor;
import util.math.Vec3d;

public class NoiseInterpolator {

    private final Noise noise;
    //private final int xSize, ySize, zSize;
    private final double[][][] samples;
    private Vec3d translate, scale;
    private int octaves;
    private double frequency;

    public NoiseInterpolator(Noise noise, int xSize, int ySize, int zSize) {
        this.noise = noise;
//        this.xSize = xSize;
//        this.ySize = ySize;
//        this.zSize = zSize;
        this.samples = new double[xSize + 1][ySize + 1][zSize + 1];
    }

    public void generate(int octaves, double frequency) {
        this.octaves = octaves;
        this.frequency = frequency;
//        for (int x = 0; x <= xSize; x++) {
//            for (int y = 0; y <= ySize; y++) {
//                for (int z = 0; z <= zSize; z++) {
//                    samples[x][y][z] = noise.fbm3d(x * scale.x + translate.x, y * scale.y + translate.y, z * scale.z + translate.z, octaves, frequency);
//                }
//            }
//        }
    }

    public double get(Vec3d v) {
        return get(v.x, v.y, v.z);
    }

    public double get(double x, double y, double z) {
        x = (x - translate.x) / scale.x;
        y = (y - translate.y) / scale.y;
        z = (z - translate.z) / scale.z;

        int x0 = floor(x);
        int x1 = ceil(x);
        int y0 = floor(y);
        int y1 = ceil(y);
        int z0 = floor(z);
        int z1 = ceil(z);

        double xd = x - x0;
        double yd = y - y0;
        double zd = z - z0;

//        double c00 = mix(samples[x0][y0][z0], samples[x1][y0][z0], xd);
//        double c10 = mix(samples[x0][y1][z0], samples[x1][y1][z0], xd);
//        double c01 = mix(samples[x0][y0][z1], samples[x1][y0][z1], xd);
//        double c11 = mix(samples[x0][y1][z1], samples[x1][y1][z1], xd);
        double c00 = mix(sampleAt(x0, y0, z0), sampleAt(x1, y0, z0), xd);
        double c10 = mix(sampleAt(x0, y1, z0), sampleAt(x1, y1, z0), xd);
        double c01 = mix(sampleAt(x0, y0, z1), sampleAt(x1, y0, z1), xd);
        double c11 = mix(sampleAt(x0, y1, z1), sampleAt(x1, y1, z1), xd);

        double c0 = mix(c00, c10, yd);
        double c1 = mix(c01, c11, yd);

        return mix(c0, c1, zd);
    }

    private static double mix(double d0, double d1, double amt) {
        return d0 * (1 - amt) + d1 * amt;
    }

    private double sampleAt(int x, int y, int z) {
        if (samples[x][y][z] == 0) {
            samples[x][y][z] = noise.fbm3d(x * scale.x + translate.x, y * scale.y + translate.y, z * scale.z + translate.z, octaves, frequency);
        }
        return samples[x][y][z];
    }

    public void setTransform(Vec3d translate, Vec3d scale) {
        this.translate = translate;
        this.scale = scale;
    }
}
